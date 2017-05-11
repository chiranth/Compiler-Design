package cop5556sp17;


import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.*;
import java.util.ArrayList;
import static cop5556sp17.Scanner.Kind.*;
import cop5556sp17.Scanner.Token;

public class Parser {
	
	ArrayList<Kind> firststatement,firstdec,firstchainElem,firstfactor,relop,weakop,strongop,filteroplist,frameoplist,imageoplist;

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}
	
	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
		firststatement =new ArrayList<Kind>();
		firstdec=new ArrayList<Kind>();
		firstchainElem=new ArrayList<Kind>();
		firstfactor=new ArrayList<Kind>();
		relop=new ArrayList<Kind>();
		weakop=new ArrayList<Kind>();
		strongop=new ArrayList<Kind>();
		filteroplist=new ArrayList<Kind>();
		frameoplist=new ArrayList<Kind>();
		imageoplist=new ArrayList<Kind>();
		initializefirst();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		
		Program p=program();
		matchEOF();
		return p;
	}

	Expression expression() throws SyntaxException {
		//TODO
		Expression e0=null;
		Expression e1=null;
		Kind kind=t.kind;
		if(firstfactor.contains(kind))
		{
			Token firsttoken = t;
			e0=term();
			kind=t.kind;
			while(relop.contains(kind))
			{
				Token op=t;
				consume();
				e1=term();
				e0=new BinaryExpression(firsttoken,e0,op,e1);
				kind=t.kind;
			}
		}
		else{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal Expression "+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal Expression ");
		}
		return e0;
	}

	Expression term() throws SyntaxException {
		//TODO
		Kind kind=t.kind;
		Expression e0=null;
		Expression e1=null;
		if(firstfactor.contains(kind))
		{
			Token firsttoken=t;
			e0=elem();
			kind=t.kind;
			while(weakop.contains(kind))
			{
				Token op=t;
				consume();
				e1=elem();
				e0=new BinaryExpression(firsttoken,e0,op,e1);
				kind=t.kind;
			}
		}
		else{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal Expression term "+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal Expression term");
		}
		return e0;
	}

	Expression elem() throws SyntaxException {
		Kind kind=t.kind;
		Expression e0=null;
		Expression e1=null;
		if(firstfactor.contains(kind))
		{
			Token firsttoken=t;
			e0=factor();
			kind=t.kind;
			while(strongop.contains(kind))
			{
				Token op=t;
				consume();
				e1=factor();
				e0=new BinaryExpression(firsttoken,e0,op,e1);
				kind=t.kind;
			}
		}
		else{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal Expression element "+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal Expression element");
		}
		return e0;
	}

	Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		Expression e0=null;
		switch (kind) {
		case IDENT: {
			e0=new IdentExpression(t);
			new IdentLValue(t);
			consume();
		}
			break;
		case INT_LIT: {
			e0=new IntLitExpression(t);
			consume();
		}
			break;
		case KW_TRUE:
		case KW_FALSE: {
			e0=new BooleanLitExpression(t);
			consume();
		}
			break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			e0=new ConstantExpression(t);
			consume();
		}
			break;
		case LPAREN: {
			consume();
			e0=expression();
			match(RPAREN);
		}
			break;
		default:
			//you will want to provide a more useful error message
			if(!t.kind.equals(EOF))
			throw new SyntaxException("illegal factor "+t.getLinePos().toString());
			else
				throw new SyntaxException("illegal factor ");
		}
		return e0;
	}

	Block block() throws SyntaxException {
		//TODO
		Block bl=null;
		ArrayList<Dec> decs=new ArrayList<Dec>();
		ArrayList<Statement> statements=new ArrayList<Statement>();
		Token firsttype=t;
		match(LBRACE);
		Kind kind=t.kind;
		while(firstdec.contains(kind)||firststatement.contains(kind))
		{
			/*if(decs.isEmpty() && statements.isEmpty())
			{
				firsttype=t;
			}*/
			if(firstdec.contains(kind))
			{
				decs.add(dec());
			}
			else if(firststatement.contains(kind))
			{
				statements.add(statement());
			}
			kind=t.kind;
		}
		match(RBRACE);
		bl=new Block(firsttype,decs,statements);
		return bl;
		
	}

	Program program() throws SyntaxException {
		//TODO
		ArrayList<ParamDec> paramlist=new ArrayList<ParamDec>();
		Block b0=null;
		Token firstkind=null;
		if(t.kind.equals(IDENT))
		{
			firstkind=t;
			consume();
			Kind kind=t.kind;
			if(kind.equals(LBRACE))
			{
				b0=block();
			}
			else if(kind.equals(KW_URL)|kind.equals(KW_FILE)|kind.equals(KW_INTEGER)|kind.equals(KW_BOOLEAN))
			{
				paramlist.add(paramDec());
				while(t.kind.equals(COMMA))
				{
					consume();
					paramlist.add(paramDec());
				}
				b0=block();
			}
			else{
				if(!t.kind.equals(EOF))
				throw new SyntaxException("Illegal Program declaration "+ t.getLinePos().toString());
				else
					throw new SyntaxException("Illegal Program declaration ");
			}
		}
		else{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal Program "+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal Program ");	
		}
		return new Program(firstkind,paramlist,b0);
		
		
	}

	ParamDec paramDec() throws SyntaxException {
		//TODO
		Kind kind=t.kind;
		ParamDec pd=null;
		if(kind.equals(KW_URL)|kind.equals(KW_FILE)|kind.equals(KW_INTEGER)|kind.equals(KW_BOOLEAN))
		{
			Token firsttoken=t;
			consume();
			Token ident=t;
			match(IDENT);
			pd=new ParamDec(firsttoken,ident);
			
		}
		else{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal Parameter declaration "+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal Parameter declaration ");
			
		}
		return pd;
	}

	Dec dec() throws SyntaxException {
		//TODO
		Dec declaration=null;
		Kind kind=t.kind;
		if(kind.equals(KW_INTEGER) || kind.equals(KW_BOOLEAN) || kind.equals(KW_IMAGE) || kind.equals(KW_FRAME))
		{
			Token firsttoken=t;
			consume();
			Token ident=t;
			match(IDENT);
			declaration=new Dec(firsttoken,ident);
		}
		else
		{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal Parameter declaration"+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal Parameter declaration");
		}
		return declaration;
	}

	Statement statement() throws SyntaxException {
		//TODO
		Kind kind=t.kind;
		Statement st=null;
		Token firsttoken;
		Expression e0;
		Block b=null;
		if(firststatement.contains(kind))
		{
			firsttoken=t;
			switch(kind)
			{
			case OP_SLEEP:  consume();
							e0=expression();
							match(SEMI);
							st=new SleepStatement(firsttoken,e0);
							break;
			case KW_WHILE:  consume();
							match(LPAREN);
							e0=expression();
							match(RPAREN);
							b=block();
							st=new WhileStatement(firsttoken,e0,b);
							break;
			case KW_IF:		consume();
							match(LPAREN);
							e0=expression();
							match(RPAREN);
							b=block();
							st=new IfStatement(firsttoken,e0,b);
							break;
			case IDENT:		Kind kind1=scanner.peek().kind;
							if(kind1.equals(ASSIGN))
							{
								IdentLValue val=new IdentLValue(t);
								consume();
								consume();
								e0=expression();
								match(SEMI);
								st=new AssignmentStatement(firsttoken,val,e0);
							}
							else if(kind1.equals(ARROW) || kind1.equals(BARARROW))
							{
								st=chain();
								match(SEMI);
							}
							else{
								if(!t.kind.equals(EOF))
								throw new SyntaxException("Illegal Parameter statement"+ t.getLinePos().toString());
								else
									throw new SyntaxException("Illegal Parameter statement");
							}
							break;
			default: {	st=chain();
						match(SEMI);
						}
			
			}
		}
		else
		{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal Parameter statement"+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal Parameter statement");
		}
		return st;
	}

	Chain chain() throws SyntaxException {
		
		//TODO
		Chain ch0=null;
		ChainElem ch1=null;
		Kind kind=t.kind;
		Token op;
		if(firstchainElem.contains(kind))
		{
			ch0=chainElem();
			op=arrowOp();
			ch1=chainElem();
			ch0=new BinaryChain(ch0.firstToken,ch0,op,ch1);
			Kind kind1=t.kind;
			while(kind1.equals(ARROW) || kind1.equals(BARARROW))
			{
				op=arrowOp();
				ch1=chainElem();
				kind1=t.kind;
				ch0=new BinaryChain(ch0.firstToken,ch0,op,ch1);
			}
			
		}
		else{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal chain statement"+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal chain statement");
		}
		return ch0;
	}

	ChainElem chainElem() throws SyntaxException {
		//TODO
		Kind kind=t.kind;
		ChainElem chele=null;
		if(firstchainElem.contains(kind))
		{
			if(kind.equals(IDENT))
			{
				chele=new IdentChain(t);
				consume();
			}
			else
			{
				Token firsttoken=t;
				consume();
				Tuple tpl=arg();
				if(filteroplist.contains(firsttoken.kind))
				{
					chele=new FilterOpChain(firsttoken,tpl);
				}
				else if(frameoplist.contains(firsttoken.kind)){
					chele=new FrameOpChain(firsttoken,tpl);
					
				}
				else if(imageoplist.contains(firsttoken.kind))
				{
					chele=new ImageOpChain(firsttoken,tpl);
				}
			}
		}
		else{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal chain statement"+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal chain statement");
		}
		return chele;
	}
	
	Token arrowOp() throws SyntaxException
	{
		Kind kind=t.kind;
		Token op=t;
		if(kind.equals(ARROW) || kind.equals(BARARROW))
		{
			consume();
		}
		else{
			if(!t.kind.equals(EOF))
			throw new SyntaxException("Illegal chain statement"+ t.getLinePos().toString());
			else
				throw new SyntaxException("Illegal chain statement");
		}
		return op;
	}

	Tuple arg() throws SyntaxException {
		//TODO
		Kind kind=t.kind;
		Tuple tl=null;
		ArrayList<Expression> exs=new ArrayList<Expression>();
		if(kind.equals(LPAREN))
		{
			Token firsttoken=consume();
			exs.add(expression());
			Kind kind1=t.kind;
			while(kind1.equals(COMMA))
			{
				consume();
				exs.add(expression());
				kind1=t.kind;
			}
			match(RPAREN);
			tl=new Tuple(firsttoken,exs);
			return tl;
		}
		else{
			return tl=new Tuple(t,exs);
		}
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.kind.equals(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.kind.equals(kind)) {
			return consume();
		}
		if(!t.kind.equals(EOF))
		throw new SyntaxException("saw " + t.kind + "expected " + kind+"at "+t.getLinePos().toString());
		else
			throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		for(Kind kind: kinds)
		{
			if (t.kind.equals(kind)) {
				return consume();
			}
		}
		
		if(!t.kind.equals(EOF))
			throw new SyntaxException(t.kind + "not expected at "+t.getLinePos().toString());
			else
				throw new SyntaxException(t.kind + "not expected");
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}
	
	void initializefirst()
	{
		
		firstdec.add(KW_INTEGER);firstdec.add(KW_BOOLEAN);firstdec.add(KW_IMAGE);firstdec.add(KW_FRAME);
		
		firststatement.add(OP_SLEEP);firststatement.add(KW_WHILE);firststatement.add(KW_IF);firststatement.add(KW_SCALE);
		firststatement.add(IDENT);firststatement.add(OP_BLUR);firststatement.add(OP_GRAY); firststatement.add(OP_CONVOLVE);
		firststatement.add(KW_SHOW);firststatement.add(KW_HIDE);firststatement.add(KW_MOVE);firststatement.add(KW_XLOC);
		firststatement.add(KW_YLOC);firststatement.add(OP_WIDTH);firststatement.add(OP_HEIGHT);
		
		firstchainElem.add(IDENT);firstchainElem.add(OP_BLUR);firstchainElem.add(OP_GRAY); firstchainElem.add(OP_CONVOLVE);
		firstchainElem.add(KW_SHOW);firstchainElem.add(KW_HIDE);firstchainElem.add(KW_MOVE);firstchainElem.add(KW_XLOC);
		firstchainElem.add(KW_YLOC);firstchainElem.add(OP_WIDTH);firstchainElem.add(OP_HEIGHT);firstchainElem.add(KW_SCALE);
		
		firstfactor.add(IDENT);firstfactor.add(INT_LIT);firstfactor.add(KW_TRUE);firstfactor.add(KW_FALSE);
		firstfactor.add(KW_SCREENWIDTH);firstfactor.add(KW_SCREENHEIGHT);firstfactor.add(LPAREN);
		
		relop.add(LT);relop.add(LE);relop.add(GT);relop.add(GE);
		relop.add(EQUAL);relop.add(NOTEQUAL);
		
		weakop.add(PLUS);weakop.add(MINUS);weakop.add(OR);
		
		strongop.add(TIMES);strongop.add(DIV);strongop.add(AND);strongop.add(MOD);
		
		filteroplist.add(OP_BLUR);filteroplist.add(OP_GRAY);filteroplist.add(OP_CONVOLVE);
		
		frameoplist.add(KW_SHOW);frameoplist.add(KW_HIDE);frameoplist.add(KW_MOVE);
		frameoplist.add(KW_XLOC);frameoplist.add(KW_YLOC);
		
		imageoplist.add(OP_WIDTH);imageoplist.add(OP_HEIGHT);imageoplist.add(KW_SCALE);
		
	}

}
