package tigase.auth;

import tigase.auth.mechanisms.SaslEXTERNAL;
import tigase.auth.mechanisms.SaslSCRAMPlus;
import tigase.auth.mechanisms.TigaseSaslServerFactory;
import tigase.cert.CertificateUtil;
import tigase.kernel.beans.Bean;
import tigase.kernel.beans.config.ConfigField;
import tigase.vhosts.VHostItem;
import tigase.xmpp.XMPPResourceConnection;

import javax.security.sasl.SaslServerFactory;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

@Bean(name = "mechanism-selector", parent = TigaseSaslProvider.class, active = true)
public class DefaultMechanismSelector implements MechanismSelector {

	@ConfigField(desc = "List of allowed SASL mechanisms", alias = "allowed-mechanisms")
	private final HashSet<String> allowedMechanisms = new HashSet<String>();
	
	@Override
	public Collection<String> filterMechanisms(Enumeration<SaslServerFactory> serverFactories, XMPPResourceConnection session) {
		final Map<String, ?> props = new HashMap<String, Object>();
		final ArrayList<String> result = new ArrayList<String>();
		while (serverFactories.hasMoreElements()) {
			SaslServerFactory ss = serverFactories.nextElement();
			String[] x = ss.getMechanismNames(props);
			for (String name : x) {
				if (match(ss, name, session) && isAllowedForDomain(name, session.getDomain()))
					result.add(name);
			}
		}
		return result;
	}
	
	protected boolean isAllowedForDomain(final String mechanismName, final VHostItem vhost) {
		final String[] saslAllowedMechanisms = vhost.getSaslAllowedMechanisms();
		if (saslAllowedMechanisms != null && saslAllowedMechanisms.length > 0) {
			for (String allowed : saslAllowedMechanisms) {
				if (allowed.equals(mechanismName))
					return true;
			}
			return false;
		} else if (!allowedMechanisms.isEmpty()) {
			return allowedMechanisms.contains(mechanismName);
		}
		return true;
	}

	private boolean isJIDInCertificate(final XMPPResourceConnection session) {
		Certificate cert = (Certificate) session.getSessionData(SaslEXTERNAL.PEER_CERTIFICATE_KEY);
		if (cert == null)
			return false;

		final List<String> authJIDs = CertificateUtil.extractXmppAddrs((X509Certificate) cert);
		return authJIDs != null && !authJIDs.isEmpty();
	}

	protected boolean match(SaslServerFactory factory, String mechanismName, XMPPResourceConnection session) {
		if (session.isTlsRequired() && !session.isEncrypted())
			return false;
		if (factory instanceof TigaseSaslServerFactory) {
			if (!session.getDomain().isAnonymousEnabled() && "ANONYMOUS".equals(mechanismName))
				return false;
			if ("EXTERNAL".equals(mechanismName) && !isJIDInCertificate(session))
				return false;
			if (SaslSCRAMPlus.NAME.equals(mechanismName) && !SaslSCRAMPlus.isAvailable(session))
				return false;
			return true;
		}
		return false;
	}
}
