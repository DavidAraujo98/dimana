grammar Grammar;
program: (pstat|use)* EOF;

pstat: stat;

stat:
	assig ';'	 		# StatAssig
	| decl ';'			# StatDeclare
	| write ';'			# StatWrite
	| appnd ';'			# StatAppend
	| loop ';'			# StatLoop
	| cond ';'			# StatConditional
	;

use:
	'use' STRING ';'	# UseModule;

type:
	'list[' type ']'	# TypeList
	| numerictype		# TypeNumeric
	| 'string'			# TypeString
	| 'bool'			# TypeBoolean
	| ID				# TypeDimension;

numerictype:
	'real'				# TypeReal
	| 'integer'			# TypeInteger
	;

decl: 
	type ID			# DeclareVariable
	| dimension		# DeclareDimension
	;

assig returns[String varName]: 
	type? ID '=' expr										# AssigVarible
	| 'unit' ID '[' unit=ID ',' suffix=ID ']' '=' expr		# AssigNonSI
	| dimension '=' expr									# AssigDependantUnits
	| 'prefix' numerictype ID '=' (REAL|INTEGER)			# AssigSIPrefix
	;

write:
	'write' expr (',' expr)*		# WriteOut
	| 'writeln' expr (',' expr)*	# WriteOutNewLine;

appnd: expr '>>' ID # AppendToListID;

loop: 'for' assig 'to' expr 'do' stat+ 'end' # LoopFor;

cond:
	'if' ( expr ) 'then' stat+ ('elif' stat*)* ('else' stat* )? 'fi' # ConditionalIf;

dimension returns [String dimName, String unitName, String suffixS]:
	'dimension' numerictype ID ('[' (unit=ID ',')? suffix=ID ']')?		# DimensionDeclare;

expr returns[String eType,String varName]:
	'string(' expr ',' INTEGER ')'								# ExprCreateString
	| 'string(' expr ')'										# ExprConvertString
	| numerictype'(' expr ')'									# ExprConvertNumeric 
	| 'length(' expr ')'										# ExprLenght
	| expr ID													# ExprPrefix
	| 'read' STRING?											# ExprReadIn
	| 'new' type												# ExprNewTypeID
	| type '(' expr ')' '*' ID									# ExprCastWithUnitName
	| op=('+' | '-') expr										# ExprUnary
	| <assoc = right> expr '^' INTEGER							# ExprExpo
	| expr op=('*' | '/' | '%') expr							# ExprMulDivRem
	| expr op=('+' | '-') expr									# ExprAddSub
	| expr op=('==' | '!=' | '<' | '>' | '>=' | '<=') expr		# ExprComparison
	| expr op=('&&' | '||') expr								# ExprAndOR
	| ID '[' expr ']'											# ExprIndexOfList
	| BOOLEAN													# ExprBoolean
	| STRING													# ExprString
	| REAL														# ExprReal
	| INTEGER													# ExprInteger
	| ID														# ExprID
	| '(' expr ')'												# ExprParent;

STRING: ('"' ( '""' | ~["])* '"');
BOOLEAN: 'true' | 'false';
INTEGER: ([0]|[1-9]+[0-9]*);
REAL: INTEGER (('.' ([0])* INTEGER)|('e' '-'? INTEGER));
ID: [a-zA-Z_]+[a-zA-Z_0-9]*;
COMMENT: '#' .*? '\n' -> skip;
WS: [ \t\r\n]+ -> skip;
