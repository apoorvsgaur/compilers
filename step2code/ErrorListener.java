public class ThrowingErrorListener extends BaseErrorListener {

	public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int 				charPositionInLine, String msg, RecognitionException e)
	throws ParseCancellationException {
		throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
      	}
}
