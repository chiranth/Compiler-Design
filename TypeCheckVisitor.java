package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;
import java.util.*;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.BARARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName chaintype=(TypeName)binaryChain.getE0().visit(this, arg);
		ChainElem chainelem=binaryChain.getE1();
		TypeName chainelemtype=(TypeName)chainelem.visit(this, arg);
		Kind op=binaryChain.getArrow().kind;
		TypeName bchaintype;

		switch(chaintype)
		{
		case URL: 	if(op.equals(ARROW) && chainelemtype.isType(IMAGE))
					{
						bchaintype=IMAGE;
						binaryChain.type=bchaintype;
					}
					else
					{
						throw new TypeCheckException("Invalid Binary chain expression");
					}
			break;
		case FILE: 	if(op.equals(ARROW) && chainelemtype.isType(IMAGE))
					{
						bchaintype=IMAGE;
						binaryChain.type=bchaintype;
					}
					else
					{
						throw new TypeCheckException("Invalid Binary chain expression");
					}
			break;
		case FRAME:	if(op.equals(ARROW) && chainelem instanceof FrameOpChain && chainelemtype.isType(INTEGER))
					{
						bchaintype=INTEGER;
						binaryChain.type=bchaintype;
					}
					else if(op.equals(ARROW) && chainelem instanceof FrameOpChain && chainelemtype.isType(NONE))
					{
						bchaintype=FRAME;
						binaryChain.type=bchaintype;
					}
					else
					{
						throw new TypeCheckException("Invalid Binary chain expression");
					}
			break;
		case IMAGE:	if(op.equals(ARROW) && chainelem instanceof ImageOpChain && chainelemtype.isType(INTEGER))
					{
						bchaintype=INTEGER;
						binaryChain.type=bchaintype;
					}
					else if(op.equals(ARROW) && chainelemtype.isType(FRAME))
					{
						bchaintype=FRAME;
						binaryChain.type=bchaintype;
					}
					else if(op.equals(ARROW) && chainelemtype.isType(FILE))
					{
						bchaintype=NONE;
						binaryChain.type=bchaintype;
					}
					else if((op.equals(ARROW) || op.equals(BARARROW) ) && chainelem instanceof FilterOpChain && chainelemtype.isType(IMAGE) &&(chainelem.getFirstToken().kind.equals(OP_BLUR)||chainelem.getFirstToken().kind.equals(OP_GRAY)||chainelem.getFirstToken().kind.equals(OP_CONVOLVE)))
					{
						bchaintype=IMAGE;
						binaryChain.type=bchaintype;
					}
					else if(op.equals(ARROW) && chainelem instanceof ImageOpChain && chainelemtype.isType(IMAGE) && chainelem.getFirstToken().kind.equals(KW_SCALE))
					{
						bchaintype=IMAGE;
						binaryChain.type=bchaintype;
					}
					else if(op.equals(ARROW) && chainelem instanceof IdentChain && chainelemtype.isType(IMAGE))
					{
						bchaintype=IMAGE;
						binaryChain.type=bchaintype;
					}
					else
					{
						throw new TypeCheckException("Invalid Binary chain expression");
					}

			break;
		case INTEGER: 	if(op.equals(ARROW) && chainelem instanceof IdentChain && chainelemtype.isType(INTEGER))
						{
							bchaintype=INTEGER;
							binaryChain.type=bchaintype;
						}
						else
						{
							throw new TypeCheckException("Invalid Binary chain expression");
						}
		break;
		default: throw new TypeCheckException("Unexpected chain type");
		}
		return bchaintype;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression e0=binaryExpression.getE0();
		Expression e1=binaryExpression.getE1();
		Kind op=binaryExpression.getOp().kind;
		TypeName btype;
		e0.visit(this, arg);
		e1.visit(this,arg);
		TypeName e0type=e0.type;
		TypeName e1type=e1.type;
		switch(e0type){
		case INTEGER:	if((op.equals(PLUS) || op.equals(MINUS) || op.equals(TIMES) || op.equals(DIV) ||op.equals(MOD)) && e1type.isType(INTEGER))
						{
							btype=INTEGER;
							binaryExpression.type=btype;
						}
						else if((op.equals(TIMES)|| op.equals(DIV) ||op.equals(MOD) )&& e1type.isType(IMAGE))
						{
							btype=IMAGE;
							binaryExpression.type=btype;
						}
						else if((op.equals(LT) || op.equals(GT) || op.equals(LE) || op.equals(GE)) && e1type.isType(INTEGER))
						{
							btype=BOOLEAN;
							binaryExpression.type=btype;
						}
						else if((op.equals(EQUAL)|| op.equals(NOTEQUAL)) && e1type.isType(e0type))
						{
							btype=BOOLEAN;
							binaryExpression.type=btype;
						}
						else
						{
							throw new TypeCheckException("Invalid Binary expression");
						}
			break;
		case IMAGE:	if((op.equals(PLUS) || op.equals(MINUS)) && e1type.isType(IMAGE))
					{
						btype=IMAGE;
						binaryExpression.type=btype;
					}
					else if((op.equals(TIMES)|| op.equals(DIV) ||op.equals(MOD)) && e1type.isType(INTEGER))
					{
						btype=IMAGE;
						binaryExpression.type=btype;
					}


					else if((op.equals(EQUAL)|| op.equals(NOTEQUAL)) && e1type.isType(e0type))
					{
						btype=BOOLEAN;
						binaryExpression.type=btype;
					}
					else
					{
						throw new TypeCheckException("Invalid Binary expression");
					}
			break;
		case BOOLEAN:	if((op.equals(LT) || op.equals(GT) || op.equals(LE) || op.equals(GE)) && e1type.isType(BOOLEAN))
						{
							btype=BOOLEAN;
							binaryExpression.type=btype;
						}
							else if((op.equals(EQUAL)|| op.equals(NOTEQUAL)) && e1type.isType(e0type))
							{
								btype=BOOLEAN;
								binaryExpression.type=btype;
							}
							else if((op.equals(AND)|| op.equals(OR)) && e1type.isType(e0type))
							{
								btype=BOOLEAN;
								binaryExpression.type=btype;
							}
						else
						{
							throw new TypeCheckException("Invalid Binary expression");
						}
			break;
		default: 	if((op.equals(EQUAL)|| op.equals(NOTEQUAL)) && e1type.isType(e0type))
					{
						btype=BOOLEAN;
						binaryExpression.type=btype;
					}
					else{
						throw new TypeCheckException("Invalid Binary Expression");
					}
		}
		return btype;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		ArrayList<Dec> decs=block.getDecs();
		ArrayList<Statement> statements=block.getStatements();

		for(ListIterator<Dec> dec = decs.listIterator();dec.hasNext();)
		{
			dec.next().visit(this, arg);

		}

		for(ListIterator<Statement> st = statements.listIterator();st.hasNext();)
		{
			st.next().visit(this, arg);

		}
		symtab.leaveScope();

		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.type=BOOLEAN;
		return BOOLEAN;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple args=filterOpChain.getArg();
		args.visit(this, arg);
		if(!args.getExprList().isEmpty())
			throw new TypeCheckException("Unexpected argument list for filterop");
		TypeName foptype=TypeName.IMAGE;
		filterOpChain.type=foptype;
		return foptype;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple args=frameOpChain.getArg();
		args.visit(this, arg);
		TypeName foptype;
		Kind frameop=frameOpChain.getFirstToken().kind;
		if(frameop.equals(KW_SHOW)||frameop.equals(KW_HIDE))
		{
			if(!args.getExprList().isEmpty())
				throw new TypeCheckException("Unexpected argument list for frameop");
			foptype=TypeName.NONE;
		}
		else if(frameop.equals(KW_XLOC)||frameop.equals(KW_YLOC))
		{
			if(!args.getExprList().isEmpty())
				throw new TypeCheckException("Unexpected argument list for frameop");
			foptype=TypeName.INTEGER;
		}
		else if(frameop.equals(KW_MOVE))
		{
			if(args.getExprList().size()!=2)
				throw new TypeCheckException("Uneven argument list for frameop: Expected 2 arguments");
			foptype=TypeName.NONE;
		}
		else{
			throw new TypeCheckException("Unexpected frame op type");
		}
		frameOpChain.type=foptype;
		return foptype;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec identdec=symtab.lookup(identChain.getFirstToken().getText());
		if(identdec==null)
			throw new TypeCheckException("Identifier not accessible/declared in current scope");
		TypeName idtype=identdec.type;
		identChain.type=idtype;
		identChain.setdec(identdec);
		return idtype;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token ident=identExpression.getFirstToken();
		Dec identdec=symtab.lookup(ident.getText());
		if(identdec==null)
			throw new TypeCheckException("Invalid expression:Identifier not avaiable in the block");
		TypeName iexptype=identdec.type;
		identExpression.type=iexptype;
		identExpression.setdec(identdec);
		return iexptype;
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression exp=ifStatement.getE();
		Block blk=ifStatement.getB();

		exp.visit(this, arg);
		blk.visit(this, arg);
		if(!exp.type.isType(BOOLEAN))
			throw new TypeCheckException("Invalid expression: Expected Boolean condition in if statement");
		return null;

	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.type=INTEGER;
		return INTEGER;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression ex=sleepStatement.getE();
		TypeName type=(TypeName)ex.visit(this, arg);
		if(!ex.type.isType(INTEGER))
			throw new TypeCheckException("Expected integer type in Sleep Statement");
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Expression exp=whileStatement.getE();
		Block blk=whileStatement.getB();

		exp.visit(this, arg);
		blk.visit(this, arg);
		if(!exp.type.isType(BOOLEAN))
			throw new TypeCheckException("Invalid expression: Expected Boolean condition in while statement");
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String ident=declaration.getIdent().getText();
		declaration.type=Type.getTypeName(declaration.getFirstToken());
		boolean success=symtab.insert(ident, declaration);
		if(!success)
			throw new TypeCheckException("Duplicate declaration in the same scope");
		return null;

	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		ArrayList<ParamDec> paramdecs=program.getParams();

		for(ListIterator<ParamDec> pd = paramdecs.listIterator();pd.hasNext();)
		{
			pd.next().visit(this, arg);
		}
		program.getB().visit(this, arg);
		return null;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		IdentLValue ival=assignStatement.getVar();
		Expression exp=assignStatement.getE();
		TypeName valtype=(TypeName)ival.visit(this, arg);
		TypeName extype=(TypeName)exp.visit(this, arg);
		if(!valtype.equals(extype))
			throw new TypeCheckException("Invalid Assignment statement: Types do not match");
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec idec=symtab.lookup(identX.getText());
		if(idec==null)
			throw new TypeCheckException("TypeCheckException: Variable not declared in the scope");

		TypeName idtype=idec.type;
		identX.setdec(idec);
		return idtype;
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String ident=paramDec.getIdent().getText();
		paramDec.type=Type.getTypeName(paramDec.getFirstToken());
		boolean success=symtab.insert(ident, paramDec);
		if(!success)
			throw new TypeCheckException("Duplicate declaration in the same scope");
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.type=INTEGER;
		return INTEGER;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Kind op=imageOpChain.getFirstToken().kind;
		Tuple args=imageOpChain.getArg();
		args.visit(this, arg);
		TypeName imageoptype;
		if(op.equals(OP_WIDTH)||op.equals(OP_HEIGHT))
		{
			if(args.getExprList().size()!=0)
				throw new TypeCheckException("Unexpected arguments list");
			imageoptype=TypeName.INTEGER;
		}
		else if(op.equals(KW_SCALE))
		{
			if(args.getExprList().size()!=1)
				throw new TypeCheckException("Unexpected arguments list");
			imageoptype=TypeName.IMAGE;
		}
		else{
			throw new TypeCheckException("Invalid Image OP type");
		}
		imageOpChain.type=imageoptype;
		return imageoptype;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<Expression> exlist=tuple.getExprList();
		Expression exp;
		for(ListIterator<Expression> expit = exlist.listIterator();expit.hasNext();)
		{
			exp=expit.next();
			exp.visit(this, arg);
			if(!exp.type.isType(INTEGER))
			{
				throw new TypeCheckException("Invalid expression in args list: Expected integer type expression");
			}

		}
		return null;
	}


}
