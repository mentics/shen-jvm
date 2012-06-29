%grammar
Goal = ListOfExpr;

ListOfExpr = ListOfExpr Expr
           | ;

Expr = Atom
     | "(" ExprList ")";

ExprList = ExprList Expr
         | ;

Atom = {Bool} BOOL
     | {Str} STRING
     | {_Int} _INT
     | {_Float} _FLOAT
     | {Symbol} SYMBOL;

%macros
alpha = [A-Za-z=*/+_?$!@~><&%'#`;:{}-];
digit = [0-9];
nzdigit = [1-9];
quotable = [^] \ ["];# | "\"";
#quotable = 

%tokens
STRING = "\"" (quotable | "\\\"")* "\"";
BOOL = "true" | "false";
SYMBOL = alpha ( alpha | digit )*;
_INT    = digit+ | digit* "." "0"*;
_FLOAT  = digit* "." digit* nzdigit+ digit*;

WS     = [ \t]+ { "Skip" };
NL     = "\r" | "\n" | "\r\n" { "NewLine", "Skip" };