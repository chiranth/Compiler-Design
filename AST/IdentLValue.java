package cop5556sp17.AST;

import cop5556sp17.Scanner.Token;

public class IdentLValue extends ASTNode {
	Dec decl;
	public IdentLValue(Token firstToken) {
		super(firstToken);
	}

	@Override
	public String toString() {
		return "IdentLValue [firstToken=" + firstToken + "]";
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitIdentLValue(this,arg);
	}

	public String getText() {
		return firstToken.getText();
	}
	public Dec getdec()
	{
		return this.decl;
	}

	public void setdec(Dec decl)
	{
		this.decl=decl;
	}

}
