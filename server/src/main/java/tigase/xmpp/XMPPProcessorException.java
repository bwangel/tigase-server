package tigase.xmpp;

import tigase.server.Packet;

/**
 * Created by bmalkow on 18.04.2017.
 */
public class XMPPProcessorException
		extends XMPPException {

	private static final long serialVersionUID = 1L;

	private Authorization errorCondition;

	private String text;

	public XMPPProcessorException(final Authorization errorCondition) {
		this(errorCondition, (String) null, (String) null);
	}

	/**
	 * @param errorCondition
	 * @param text human readable message will be send to client
	 */
	public XMPPProcessorException(Authorization errorCondition, String text) {
		this(errorCondition, text, (String) null);
	}

	public XMPPProcessorException(Authorization errorCondition, String text, Throwable cause) {
		this(errorCondition, text, (String) null, cause);
	}

	/**
	 * @param errorCondition
	 * @param message exception message for logging
	 * @param text human readable message will be send to client
	 */
	public XMPPProcessorException(Authorization errorCondition, String text, String message) {
		this(errorCondition, text, message, null);
	}

	public XMPPProcessorException(Authorization errorCondition, String text, String message, Throwable cause) {
		super(message, cause);
		this.errorCondition = errorCondition;
		this.text = text;
	}

	/**
	 * @return Returns the code.
	 */
	public String getCode() {
		return String.valueOf(this.errorCondition.getErrorCode());
	}

	public Authorization getErrorCondition() {
		return errorCondition;
	}

	protected String getErrorMessagePrefix() {
		return "XMPP error condition: ";
	}

	@Override
	public String getMessage() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getErrorMessagePrefix());
		sb.append(errorCondition.getCondition()).append(" ");
		if (text != null) {
			sb.append("with message: \"").append(text).append("\" ");
		}
		if (super.getMessage() != null) {
			sb.append("(").append(super.getMessage()).append(") ");
		}

		return sb.toString();
	}

	public String getMessageWithPosition() {
		final StringBuilder sb = new StringBuilder();
		sb.append(getMessage());

		StackTraceElement[] stack = getStackTrace();
		if (stack.length > 0) {
			sb.append("generated by ");
			sb.append(getStackTrace()[0].toString());
			sb.append(" ");
		}

		return sb.toString();
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return errorCondition.getCondition();
	}

	public String getText() {
		return text;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return errorCondition.getErrorType();
	}

	public Packet makeElement(Packet packet, boolean insertOriginal) throws PacketErrorTypeException {
		Packet result = errorCondition.getResponseMessage(packet, text, insertOriginal);
		return result;
	}
}
