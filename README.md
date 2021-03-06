# Compiler-Design
Implementation of Compiler for a small language
Below contains details about the language implemented
Tokens:
===========================================================
comment ::=   /*   NOT(*/)*  */
token ::= ident  | keyword | frame_op_keyword | filter_op_keyword | image_op_keyword | boolean_literal
 	| int_literal  | separator  | operator
ident ::= ident_start  ident_part*    (but not reserved)
ident_start ::=  A .. Z | a .. z | $ | _
ident_part ::= ident_start | ( 0 .. 9 )
int_literal ::= 0  |  (1..9) (0..9)*
keyword ::= integer | boolean | image | url | file | frame | while | if | sleep | screenheight | screenwidth 
filter_op_keyword ∷= gray | convolve | blur | scale
image_op_keyword ∷= width | height 
frame_op_keyword ∷= xloc | yloc | hide | show | move
boolean_literal ::= true | false
separator ::= 	;  | ,  |  (  |  )  | { | }
operator ::=   	|  | &  |  ==  | !=  | < |  > | <= | >= | +  |  -  |  *   |  /   |  % | !  | -> |  |-> | <-

Attribute free grammar
==========================================================
program ::=  IDENT block
program ::=  IDENT param_dec ( , param_dec )*   block
paramDec ::= ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN)   IDENT
block ::= { ( dec | statement) * }
dec ::= (  KW_INTEGER | KW_BOOLEAN | KW_IMAGE | KW_FRAME)    IDENT
statement ::=   OP_SLEEP expression ; | whileStatement | ifStatement | chain ; | assign ;
assign ::= IDENT ASSIGN expression
chain ::=  chainElem arrowOp chainElem ( arrowOp  chainElem)*
whileStatement ::= KW_WHILE ( expression ) block
ifStatement ::= KW_IF ( expression ) block
arrowOp ∷= ARROW   |   BARARROW
chainElem ::= IDENT | filterOp arg | frameOp arg | imageOp arg
filterOp ::= OP_BLUR |OP_GRAY | OP_CONVOLVE
frameOp ::= KW_SHOW | KW_HIDE | KW_MOVE | KW_XLOC |KW_YLOC
imageOp ::= OP_WIDTH |OP_HEIGHT | KW_SCALE
arg ::= ε | ( expression (   ,expression)* )
expression ∷= term ( relOp term)*
term ∷= elem ( weakOp  elem)*
elem ∷= factor ( strongOp factor)*
factor ∷= IDENT | INT_LIT | KW_TRUE | KW_FALSE
       	| KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )
relOp ∷=  LT | LE | GT | GE | EQUAL | NOTEQUAL 
weakOp  ∷= PLUS | MINUS | OR   
strongOp ∷= TIMES | DIV | AND | MOD

Abstract syntax
===========================================================
Program ∷= List<ParamDec> Block
ParamDec ∷= type ident
Block ∷= List<Dec>  List<Statement>
Dec ∷= type ident
Statement ∷= SleepStatement | WhileStatement | IfStatement | Chain
      	| AssignmentStatement
SleepStatement ∷= Expression
AssignmentStatement ∷= IdentLValue Expression
Chain ∷= ChainElem | BinaryChain
ChainElem ::= IdentChain | FilterOpChain | FrameOpChain | ImageOpChain
IdentChain ∷= ident
FilterOpChain ∷= filterOp Tuple
FrameOpChain ∷= frameOp Tuple
ImageOpChain ∷= imageOp Tuple
BinaryChain ∷= Chain (arrow | bararrow)  ChainElem
WhileStatement ∷= Expression Block
IfStatement ∷= Expression Block
Expression ∷= IdentExpression | IntLitExpression | BooleanLitExpression
  	| ConstantExpression | BinaryExpression
IdentExpression ∷= ident
IdentLValue ∷= ident
IntLitExpression ∷= intLit
BooleanLitExpression ∷= booleanLiteral
ConstantExpression ∷= screenWidth | screenHeight
BinaryExpression ∷= Expression op Expression
Tuple :≔ List<Expression>
	op ∷= relOp | weakOp | strongOp
type ∷= integer | image | frame | file | boolean | url

