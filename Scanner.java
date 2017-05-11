package cop5556sp17;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Collections;



public class Scanner {
	/**
	 * Kind enum
	 */
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;  

		//returns the text of this Token
		public String getText() {
			//TODO IMPLEMENT THIS
			if(length==0)
				return "eof";
			return chars.substring(pos, pos+length);
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			int index= Collections.binarySearch(linetostart, pos);
			index=index<0?-(index)-2 : index;
			LinePos lnp=new LinePos(index,pos-linetostart.get(index));
			return lnp;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException{
			//TODO IMPLEMENT THIS
			return Integer.parseInt(chars.substring(pos, pos+length));
		}
		
		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }

		 

		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
		
	}

	 


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		linetostart=new ArrayList<Integer>();

	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
		//TODO IMPLEMENT THIS!!!!
		int len=chars.length();
		String state="START";
		int ch;
		int startpos=0;
		HashMap<String,String> enmap=new HashMap<>();
		for(Kind type:Kind.values())
		{
			enmap.put(type.getText(),type.name());
		}
		if(len>0)
			linetostart.add(0);
		while(pos<=len)
		{
			if(pos<len)
				ch=chars.charAt(pos);
			else
				ch=-1;
		switch(state)
		{
			case "START": { pos=skipwhitespace(pos);
							if(pos<len)
								ch=chars.charAt(pos);
							else
								ch=-1;
							startpos=pos;
							switch(ch)
							{
							case -1: {tokens.add(new Token(Kind.EOF, pos, 0)); pos++;}
								break;
							case '+': {tokens.add(new Token(Kind.PLUS, startpos, 1));pos++;
										state="START";}
								break;
							case '-': {state="AFTER_SUB";
										pos++;
										}
								break;
							case '*': { /*if(closed)
										{
											state="AFTER_STAR";
											pos++;
										}
										else{*/
											tokens.add(new Token(Kind.TIMES, startpos, 1));pos++;
											state="START";
										//}
								
										}
								break;
							case '/': {state="AFTER_SLASH";
										pos++;
										}
								break;
							case '=': {state="AFTER_EQ";
										pos++;
										}
								break;
							case '!': {state="AFTER_NOT";
										pos++;
										}
								break;
							case '|': {state="AFTER_OR";
										pos++;
										}
								break;
							case '&': {tokens.add(new Token(Kind.AND, startpos, 1));pos++;
										state="START";}
								break;
							case '<': {state="AFTER_LT";
										pos++;
										}
								break;
							case '>': {state="AFTER_GT";
										pos++;
										}
								break;
							case '%': {tokens.add(new Token(Kind.MOD, startpos, 1));pos++;
										state="START";}
								break;
							case '0': {tokens.add(new Token(Kind.INT_LIT, startpos, 1));pos++;
										state="START";}
								break;
							case ';': {tokens.add(new Token(Kind.SEMI, startpos, 1));
										pos++;
										}break;
										
							case ',': {tokens.add(new Token(Kind.COMMA, startpos, 1));pos++;
										state="START";}
								break;
							case '(': {tokens.add(new Token(Kind.LPAREN, startpos, 1));pos++;
										state="START";}
								break;
							case ')':  {tokens.add(new Token(Kind.RPAREN, startpos, 1));pos++;
										state="START";}
								break;
							case '{': {tokens.add(new Token(Kind.LBRACE, startpos, 1));pos++;
										state="START";}
								break;
							case '}': {tokens.add(new Token(Kind.RBRACE, startpos, 1));pos++;
										state="START";}
								break;
							default: { if(Character.isDigit(ch))
										{
										state="IN_DIGIT";
										pos++;
										}
										else if (Character.isJavaIdentifierStart(ch)) {
											state = "IN_IDENT";pos++;
										} 
										else {
											int index= Collections.binarySearch(linetostart, pos);
											index=index<0?-(index)-2 : index;
											LinePos lnp=new LinePos(index,pos-linetostart.get(index));
											throw new IllegalCharException(
				                        "IllegalCharException " +chars.substring(pos,pos+1)+" at "+lnp.toString());
										}
								
							}
						
							}
				
							}break;
			case "IN_DIGIT": { if(Character.isDigit(ch))
								{
									pos++;
								}
								else
								{
									try{
										int i=Integer.parseInt(chars.substring(startpos, pos));
										tokens.add(new Token(Kind.INT_LIT, startpos, pos - startpos));
									}
									catch(NumberFormatException ne)
									{
										int index= Collections.binarySearch(linetostart, pos);
										index=index<0?-(index)-2 : index;
										LinePos lnp=new LinePos(index,pos-linetostart.get(index));
										throw new IllegalNumberException("IllegalNumberException at "+lnp.toString());
									}
									finally{
										state="START";
									}
								}
				
							}break;
			case "IN_IDENT": { if(Character.isJavaIdentifierPart(ch)){
								pos++;
								
								}
								else{
									String ident=chars.substring(startpos,pos);
									if(enmap.containsKey(ident))
									{
										Kind k=Kind.valueOf(enmap.get(ident));
										
										tokens.add(new Token(k, startpos, pos-startpos));
										state="START";
									}
									else{
										tokens.add(new Token(Kind.IDENT, startpos, pos - startpos));
										state="START";
									}
									
									
								}
				
								}break;
			
			case "AFTER_EQ": { if(ch=='=')
								{
									pos++;
									tokens.add(new Token(Kind.EQUAL, startpos, pos-startpos));
									state="START";
								
								}
								else{
									int index= Collections.binarySearch(linetostart, pos);
									index=index<0?-(index)-2 : index;
									LinePos lnp=new LinePos(index,pos-linetostart.get(index));
									throw new IllegalCharException(
					                        "IllegalCharException " +chars.substring(pos-1,pos)+" at "+lnp.toString());
				
									}
				
								}break;
								
			case "AFTER_SLASH": { if(ch=='*')
								{
									closed=false;
									pos++;
									pos=scancomment(pos);
									state="START";
				
								}
								else{
									tokens.add(new Token(Kind.DIV, startpos, pos-startpos));
									state="START";
								}
				
								}break;
				
			case "AFTER_LT": { if(ch=='=')
								{
									pos++;
									tokens.add(new Token(Kind.LE, startpos, pos-startpos));
									state="START";
									
								}
								else if(ch=='-')
								{
									pos++;
									tokens.add(new Token(Kind.ASSIGN, startpos, pos-startpos));
									state="START";
									
								}
								else
								{
									tokens.add(new Token(Kind.LT, startpos, 1));
									state="START";
									
								}
				
								}break;
				
			case "AFTER_GT": { if(ch=='=')
								{
									pos++;
									tokens.add(new Token(Kind.GE, startpos, pos-startpos));
									state="START";
									
								}
								else
								{
									tokens.add(new Token(Kind.GT, startpos, 1));
									state="START";
				
								}

								}break;
				
			case "AFTER_NOT": { if(ch=='=')
								{
									pos++;
									tokens.add(new Token(Kind.NOTEQUAL, startpos, pos-startpos));
									state="START";
									
								}
								else
								{
									tokens.add(new Token(Kind.NOT, startpos, 1));
									state="START";

								}

								}break;
				
			case "AFTER_SUB": { if(ch=='>')
								{
									pos++;
									tokens.add(new Token(Kind.ARROW, startpos, pos-startpos));
									state="START";
									
								}
								else
								{
									tokens.add(new Token(Kind.MINUS, startpos, 1));
									state="START";
								}
				
								}break;
				
			case "AFTER_OR": { if(ch=='-')
								{
									pos++;
									if(pos<len)
										ch=chars.charAt(pos);
									else
										ch=-1;
									if(ch=='>')
									{
										pos++;
										tokens.add(new Token(Kind.BARARROW, startpos, pos-startpos));
										state="START";
									}
									else
									{
										pos--;
										tokens.add(new Token(Kind.OR, startpos, 1));
										state="START";
									}
								}
								else
								{
									tokens.add(new Token(Kind.OR, startpos, 1));
									state="START";
								}
				
								}break;
				
		//	case "AFTER_STAR": {if(ch=='/')
				//				{
					//				int index= Collections.binarySearch(linetostart, --pos);
						//			index=index<0?-(index)-2 : index;
							//		LinePos lnp=new LinePos(index,pos-linetostart.get(index));
								//	throw new IllegalCharException(
                        //"IllegalCharException illegal Comment End */" +ch+" at pos "+lnp.toString());
							//	}
								//else
								//{
									//tokens.add(new Token(Kind.TIMES, startpos, 1));
									//state="START";
							//	}
								//}break;
				
				
			
			default: { assert false;
				
			}
							
		}
		}
		
		tokens.add(new Token(Kind.EOF,pos,0));
		return this;  
	}
	
	public int skipwhitespace(int pos)
	{
		int ch;
		while(pos<=chars.length())
		{
			if(pos<chars.length())
				ch=chars.charAt(pos);
			else
				ch=-1;
			
			if(Character.isWhitespace(ch))
			{
				if(ch=='\n')
				{
					linenum++;
					linetostart.add(pos+1);
				}
				pos++;
				//ch=chars.charAt(pos);
			}
			else
				break;
		}
		return pos;
	}
	public int scancomment(int pos) throws IllegalCharException
	{
		int ch,start=pos-1;
		boolean prestar=false;
		while(pos<=chars.length())
		{
			if(pos<chars.length())
				ch=chars.charAt(pos);
			else
				ch=-1;
			
			if(ch=='*')
			{
				prestar=true;
				pos++;
			}
			else if(ch==-1)
			{
				
				return pos;
				//tokens.add(new Token(Kind.EOF, pos, 0)); pos++;
				/*int index= Collections.binarySearch(linetostart, start);
				index=index<0?-(index)-2 : index;
				LinePos lnp=new LinePos(index,pos-linetostart.get(index));
				throw new IllegalCharException(
                        "IllegalCharException : No comment end which started at "+lnp.toString());*/
				
			}
			else{
				if(prestar)
				{
					if(ch=='/')
					{
						closed=true;
						pos++;
						break;
					}
					else{
						if(ch=='\n')
						{
							linenum++;
							linetostart.add(pos+1);
						}
						prestar=false;
						pos++;
					}
				}
				else{
					if(ch=='\n')
					{
						linenum++;
						linetostart.add(pos+1);
					}
					pos++;
				}
				
			}
				
		}
		return pos;
	}



	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum,linenum=0;
	final ArrayList<Integer> linetostart;
	boolean closed=false;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {
		//TODO IMPLEMENT THIS
		return t.getLinePos();
	}


}
