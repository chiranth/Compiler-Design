package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.INTEGER;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	private int slot=0;


	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);

		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();

		int i=0;
		for (ParamDec dec : params)
		{
			//mv.visitVarInsn(ALOAD, 0);
			//mv.visitVarInsn(ALOAD, 1);
			//mv.visitLdcInsn(new Integer(i));

			//mv.visitInsn(AALOAD);

			dec.visit(this, i);
			i++;
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();


		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
//TODO  visit the local variables
		ArrayList<Dec> decls=program.getB().getDecs();
		for (Dec decl: decls)
		{
			String localfieldname=decl.getIdent().getText();
			String localfieldtype=decl.getTypeName().getJVMTypeDesc();
			int slotnum=decl.getslot();
		mv.visitLocalVariable(localfieldname, localfieldtype, null, startRun, endRun, slotnum);
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Chain c0=binaryChain.getE0();
		ChainElem c1=binaryChain.getE1();
		String type0=c0.getTypeName().getJVMTypeDesc();
		String type1=c1.getTypeName().getJVMTypeDesc();
		c0.visit(this, true);
		switch(type0)
		{
		case "Ljava/net/URL;":

		mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL", PLPRuntimeImageIO.readFromURLSig, false);
			break;
		case "Ljava/io/File;":mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile", PLPRuntimeImageIO.readFromFileDesc, false);
			break;
		default:
		}

		mv.visitInsn(DUP);

		c1.visit(this, false);


		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
      //TODO  Implement this
		Expression e0=binaryExpression.getE0();
		Expression e1=binaryExpression.getE1();
		e0.visit(this, arg);
		e1.visit(this, arg);
		String type0=e0.getTypeName().getJVMTypeDesc();
		String type1=e1.getTypeName().getJVMTypeDesc();
		Token op=binaryExpression.getOp();
		switch(type0)
		{
		case "I": 	if(type1=="I")
					{
						if(op.kind.equals(PLUS))
						{
							Label iplus=new Label();
							mv.visitInsn(IADD);
						}
						else if(op.kind.equals(MINUS))
						{
							mv.visitInsn(ISUB);
						}
						else if(op.kind.equals(TIMES))
						{
							mv.visitInsn(IMUL);
						}
						else if(op.kind.equals(DIV)){
							mv.visitInsn(IDIV);
						}
						else if(op.kind.equals(MOD))
						{
							mv.visitInsn(IREM);
						}
						else if(op.kind.equals(LT))
						{
							Label ilt=new Label();
							Label ilt1=new Label();
							mv.visitJumpInsn(IF_ICMPGE, ilt);
							mv.visitLdcInsn(true);
							mv.visitJumpInsn(GOTO, ilt1);
							mv.visitLabel(ilt);
							mv.visitLdcInsn(false);
							mv.visitLabel(ilt1);
						}
						else if(op.kind.equals(GT))
						{
							Label igt=new Label();
							Label igt1=new Label();
							mv.visitJumpInsn(IF_ICMPLE, igt);
							mv.visitLdcInsn(true);
							mv.visitJumpInsn(GOTO, igt1);
							mv.visitLabel(igt);
							mv.visitLdcInsn(false);
							mv.visitLabel(igt1);
						}
						else if(op.kind.equals(LE))
						{
							Label ile=new Label();
							Label ile1=new Label();
							mv.visitJumpInsn(IF_ICMPGT, ile);
							mv.visitLdcInsn(true);
							mv.visitJumpInsn(GOTO, ile1);
							mv.visitLabel(ile);
							mv.visitLdcInsn(false);
							mv.visitLabel(ile1);
						}
						else if(op.kind.equals(GE))
						{
							Label ige=new Label();
							Label ige1=new Label();
							mv.visitJumpInsn(IF_ICMPLT, ige);
							mv.visitLdcInsn(true);
							mv.visitJumpInsn(GOTO, ige1);
							mv.visitLabel(ige);
							mv.visitLdcInsn(false);
							mv.visitLabel(ige1);
						}
						else if(op.kind.equals(EQUAL))
						{
							Label ie=new Label();
							Label ie1=new Label();
							mv.visitJumpInsn(IF_ICMPNE, ie);
							mv.visitLdcInsn(true);
							mv.visitJumpInsn(GOTO, ie1);
							mv.visitLabel(ie);
							mv.visitLdcInsn(false);
							mv.visitLabel(ie1);
						}
						else if(op.kind.equals(NOTEQUAL))
						{
							Label ine=new Label();
							Label ine1=new Label();
							mv.visitJumpInsn(IF_ICMPEQ, ine);
							mv.visitLdcInsn(true);
							mv.visitJumpInsn(GOTO, ine1);
							mv.visitLabel(ine);
							mv.visitLdcInsn(false);
							mv.visitLabel(ine1);
						}
						else if(op.kind.equals(AND))
						{
							mv.visitInsn(IAND);
						}
						else{
							mv.visitInsn(IOR);
						}
					}
					else
					{
						if(op.kind.equals(TIMES))
						{
							mv.visitInsn(SWAP);
							mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
						}
						else if(op.kind.equals(DIV))
						{
							mv.visitInsn(SWAP);
							mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
						}
						else{
							mv.visitInsn(SWAP);
							mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
						}
					}
			break;
		case "Z": 	if(op.kind.equals(LT))
					{
						Label blt=new Label();
						Label blt1=new Label();
						mv.visitJumpInsn(IF_ICMPGE, blt);
						mv.visitLdcInsn(true);
						mv.visitJumpInsn(GOTO, blt1);
						mv.visitLabel(blt);
						mv.visitLdcInsn(false);
						mv.visitLabel(blt1);
					}
					else if(op.kind.equals(GT))
					{
						Label bgt=new Label();
						Label bgt1=new Label();
						mv.visitJumpInsn(IF_ICMPLE, bgt);
						mv.visitLdcInsn(true);
						mv.visitJumpInsn(GOTO, bgt1);
						mv.visitLabel(bgt);
						mv.visitLdcInsn(false);
						mv.visitLabel(bgt1);
					}
					else if(op.kind.equals(LE))
					{
						Label ble=new Label();
						Label ble1=new Label();
						mv.visitJumpInsn(IF_ICMPGT, ble);
						mv.visitLdcInsn(true);
						mv.visitJumpInsn(GOTO, ble1);
						mv.visitLabel(ble);
						mv.visitLdcInsn(false);
						mv.visitLabel(ble1);
					}
					else if(op.kind.equals(GE))
					{
						Label bge=new Label();
						Label bge1=new Label();
						mv.visitJumpInsn(IF_ICMPLT, bge);
						mv.visitLdcInsn(true);
						mv.visitJumpInsn(GOTO, bge1);
						mv.visitLabel(bge);
						mv.visitLdcInsn(false);
						mv.visitLabel(bge1);
					}
					else if(op.kind.equals(EQUAL))
					{
						Label be=new Label();
						Label be1=new Label();
						mv.visitJumpInsn(IF_ICMPNE, be);
						mv.visitLdcInsn(true);
						mv.visitJumpInsn(GOTO, be1);
						mv.visitLabel(be);
						mv.visitLdcInsn(false);
						mv.visitLabel(be1);
					}
					else if(op.kind.equals(NOTEQUAL))
					{
						Label bne=new Label();
						Label bne1=new Label();
						mv.visitJumpInsn(IF_ICMPEQ, bne);
						mv.visitLdcInsn(true);
						mv.visitJumpInsn(GOTO, bne1);
						mv.visitLabel(bne);
						mv.visitLdcInsn(false);
						mv.visitLabel(bne1);
					}
					else if(op.kind.equals(AND))
					{
						mv.visitInsn(IAND);
					}
					else{
						mv.visitInsn(IOR);
					}

			break;
		case "Ljava/awt/image/BufferedImage;": if(type1=="I")
												{
													if(op.kind.equals(TIMES))
													{

														mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mul", PLPRuntimeImageOps.mulSig, false);
													}
													else if(op.kind.equals(DIV))
													{
														mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "div", PLPRuntimeImageOps.divSig, false);
													}
													else{
														mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "mod", PLPRuntimeImageOps.modSig, false);
													}
												}
												else{
													if(op.kind.equals(PLUS))
													{
														mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "add", PLPRuntimeImageOps.addSig, false);
													}
													else if(op.kind.equals(MINUS))
													{
														mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "sub", PLPRuntimeImageOps.subSig, false);
													}
													else if(op.kind.equals(EQUAL))
													{
														Label ime=new Label();
														Label ime1=new Label();
														mv.visitJumpInsn(IF_ACMPNE, ime);
														mv.visitLdcInsn(true);
														mv.visitJumpInsn(GOTO, ime1);
														mv.visitLabel(ime);
														mv.visitLdcInsn(false);
														mv.visitLabel(ime1);
													}
													else if(op.kind.equals(NOTEQUAL))
													{
														Label imne=new Label();
														Label imne1=new Label();
														mv.visitJumpInsn(IF_ACMPEQ, imne);
														mv.visitLdcInsn(true);
														mv.visitJumpInsn(GOTO, imne1);
														mv.visitLabel(imne);
														mv.visitLdcInsn(false);
														mv.visitLabel(imne1);
													}

												}
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this

		Label blockStart = new Label();

		mv.visitLabel(blockStart);
		ArrayList<Dec> decls = block.getDecs();
		ArrayList<Statement> sts=block.getStatements();

		for (Dec decl : decls)
		{

			decl.visit(this, arg);
		}
		for (Statement st : sts)
		{
			if(st instanceof AssignmentStatement)
			{
				AssignmentStatement ast=(AssignmentStatement)st;
				if(ast.getVar().getdec().getslot()==-1)
					mv.visitVarInsn(ALOAD, 0);
			}
			st.visit(this, arg);
			if(st instanceof BinaryChain)
			{
				mv.visitInsn(POP);
			}
		}
		Label blockEnd = new Label();
		mv.visitLabel(blockEnd);
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		if(booleanLitExpression.getValue())
			mv.visitInsn(ICONST_1);
		else
			mv.visitInsn(ICONST_0);
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		Kind ex_kind=constantExpression.getFirstToken().kind;
		if(ex_kind==KW_SCREENWIDTH)
		{
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		}
		else if(ex_kind==KW_SCREENHEIGHT){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);
		}

		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this

		declaration.setslot(++slot);
		String typename=declaration.getTypeName().getJVMTypeDesc();
		if(typename.equals("Ljava/awt/image/BufferedImage;") || typename.equals("Lcop5556sp17/PLPRuntimeFrame;"))
		{
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getslot());
		}

		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		mv.visitInsn(POP);
		Kind op=filterOpChain.getFirstToken().kind;

		mv.visitInsn(ACONST_NULL);

		if(op.equals(OP_CONVOLVE))
		{
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
		}
		else if(op.equals(OP_BLUR))
		{
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
		}
		else{
			mv.visitInsn(POP);
            mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
		}

		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		Kind op=frameOpChain.getFirstToken().kind;
		frameOpChain.getArg().visit(this, arg);
		if(op.equals(KW_SHOW))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
		}
		else if(op.equals(KW_HIDE))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		}
		else if(op.equals(KW_MOVE))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		}
		else if(op.equals(KW_XLOC))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		}
		else{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
		}

		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Boolean is_left=(Boolean)arg;
		Dec dec=identChain.getdec();
		String fieldName=dec.getIdent().getText();
		String fieldType=dec.getTypeName().getJVMTypeDesc();
		String typename=identChain.getTypeName().getJVMTypeDesc();
		int slotnum=dec.getslot();
		if(is_left)
		{

			if(slotnum==-1)
			{
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, fieldName, fieldType);
			}
			else
			{


				switch(typename){

				case "I":mv.visitVarInsn(ILOAD, slotnum);
					break;
				case "Ljava/awt/image/BufferedImage;":
				case "Lcop5556sp17/PLPRuntimeFrame;":
				case "Ljava/net/URL;":
				case "Ljava/io/File;":
				default:mv.visitVarInsn(ALOAD, slotnum);

				}

			}
		}
		else{

			if(slotnum==-1)
			{
				//mv.visitVarInsn(ALOAD, 0);
				if(typename.equals("Ljava/io/File;"))
				{
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, fieldName, fieldType);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);
				}
				else{
					mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);}

			}
			else
			{

				switch(fieldType)
				{

				case "I":mv.visitVarInsn(ISTORE, slotnum);
					break;
					//new changes
				case "Ljava/awt/image/BufferedImage;": mv.visitVarInsn(ASTORE, slotnum);
				break;
				//to do
				case "Lcop5556sp17/PLPRuntimeFrame;":mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, slotnum);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);
					mv.visitInsn(DUP);
					mv.visitVarInsn(ASTORE, slotnum);
					break;

				case "Ljava/io/File;":mv.visitInsn(POP);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, className, fieldName,fieldType);
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write", PLPRuntimeImageIO.writeImageDesc, false);

					break;

				default:

				}

			}

		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		Dec dec=identExpression.getdec();
		String fieldName=dec.getIdent().getText();
		String fieldType=dec.getTypeName().getJVMTypeDesc();
		int slotnum=dec.getslot();
		if(slotnum==-1)
		{
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, fieldName, fieldType);
		}
		else
		{
			String type=identExpression.type.getJVMTypeDesc();
			switch(type){
			case "I":
			case "Z":mv.visitVarInsn(ILOAD, slotnum);
				break;


			default:mv.visitVarInsn(ALOAD, slotnum);

			}

		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		Dec dec=identX.getdec();
		String fieldName=dec.getIdent().getText();
		String fieldType=dec.getTypeName().getJVMTypeDesc();
		int slotnum=dec.getslot();
		if(slotnum==-1)
		{
			mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
		}
		else
		{

			switch(fieldType)
			{
			case "I":
			case "Z":mv.visitVarInsn(ISTORE, slotnum);
				break;
			case "Ljava/awt/image/BufferedImage;": mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage", PLPRuntimeImageOps.copyImageSig,false);

			default:mv.visitVarInsn(ASTORE, slotnum);

			}

		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		Expression e=ifStatement.getE();
		e.visit(this, arg);
		Label iflabel=new Label();
		mv.visitJumpInsn(IFEQ, iflabel);
		Block b=ifStatement.getB();
		b.visit(this, arg);
		mv.visitLabel(iflabel);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		Kind op=imageOpChain.getFirstToken().kind;
		imageOpChain.getArg().visit(this, arg);
		if(op.equals(OP_WIDTH))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getWidth", PLPRuntimeImageOps.getWidthSig, false);
		}
		else if(op.equals(OP_HEIGHT))
		{
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeImageIO.BufferedImageClassName, "getHeight", PLPRuntimeImageOps.getHeightSig, false);
		}
		else if(op.equals(KW_SCALE))
		{
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		}

		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this
		mv.visitLdcInsn(intLitExpression.getFirstToken().intVal());
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		int i=(Integer)arg;
		String fieldName=paramDec.getIdent().getText();
		String fieldType=paramDec.getTypeName().getJVMTypeDesc();
		FieldVisitor fv=cw.visitField(ACC_PUBLIC, fieldName, fieldType, null, null);
		fv.visitEnd();

		paramDec.setslot(-1);
		switch(fieldType)
		{
		case "I": mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(new Integer(i));

			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);

					mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
			break;
		case "Z": mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(new Integer(i));

		mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
					mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
			break;
		case "Ljava/io/File;":
								mv.visitVarInsn(ALOAD, 0);
								mv.visitTypeInsn(NEW, "java/io/File");
								mv.visitInsn(DUP);
								mv.visitVarInsn(ALOAD, 1);
								mv.visitLdcInsn(new Integer(i));
								mv.visitInsn(AALOAD);
								mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
								mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
			break;
		case "Ljava/net/URL;": mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitLdcInsn(new Integer(i));

		//mv.visitInsn(AALOAD);
			//mv.visitInsn(ICONST_0);
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, fieldName, fieldType);
			break;
		default:
		}


		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Expression e=sleepStatement.getE();
		e.visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> exlist=tuple.getExprList();
		Expression exp;
		for(ListIterator<Expression> expit = exlist.listIterator();expit.hasNext();)
		{
			exp=expit.next();
			exp.visit(this, arg);

		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this
		Expression e;
		Block b;
		Label checkexp=new Label();
		Label blocklabel=new Label();
		mv.visitJumpInsn(GOTO, checkexp);
		mv.visitLabel(blocklabel);
		b=whileStatement.getB();
		b.visit(this, arg);
		mv.visitLabel(checkexp);
		e=whileStatement.getE();
		e.visit(this, arg);
		mv.visitJumpInsn(IFNE, blocklabel);
		return null;

	}

}
