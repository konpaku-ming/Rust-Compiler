package ast

enum class TokenType {
    AS,
    BREAK,
    CONST,
    CONTINUE,
    CRATE,
    ELSE,
    ENUM,
    EXTERN,
    FALSE,
    FN,
    FOR,
    IF,
    IMPL,
    IN,
    LET,
    LOOP,
    MATCH,
    MOD,
    MOVE,
    MUT,
    PUB,
    REF,
    RETURN,
    SELF,
    SELF_CAP,
    STATIC,
    STRUCT,
    SUPER,
    TRAIT,
    TRUE,
    TYPE,
    UNSAFE,
    USE,
    WHERE,
    WHILE,
    AWAIT,
    DYN,
    ABSTRACT,
    BECOME,
    BOX,
    DO,
    FINAL,
    MACRO,
    OVERRIDE,
    PRIV,
    TYPEOF,
    UNSIZED,
    VIRTUAL,
    YIELD,
    TRY,
    GEN,

    IDENTIFIER,
    WHITESPACE,
    INTEGER_LITERAL,
    CHAR_LITERAL,
    STRING_LITERAL,
    OPERATOR,
    RANGE_PATH,
    CONTROL,
    DELIMITER,
}

val tokenPatterns: List<Pair<Regex, TokenType>> = listOf(
    Regex("""^\s+""") to TokenType.WHITESPACE,
    Regex("^as") to TokenType.AS,
    Regex("^break") to TokenType.BREAK,
    Regex("^const") to TokenType.CONST,
    Regex("^continue") to TokenType.CONTINUE,
    Regex("^crate") to TokenType.CRATE,
    Regex("^else") to TokenType.ELSE,
    Regex("^enum") to TokenType.ENUM,
    Regex("^extern") to TokenType.EXTERN,
    Regex("^false") to TokenType.FALSE,
    Regex("^fn") to TokenType.FN,
    Regex("^for") to TokenType.FOR,
    Regex("^if") to TokenType.IF,
    Regex("^impl") to TokenType.IMPL,
    Regex("^in") to TokenType.IN,
    Regex("^let") to TokenType.LET,
    Regex("^loop") to TokenType.LOOP,
    Regex("^match") to TokenType.MATCH,
    Regex("^mod") to TokenType.MOD,
    Regex("^move") to TokenType.MOVE,
    Regex("^mut") to TokenType.MUT,
    Regex("^pub") to TokenType.PUB,
    Regex("^ref") to TokenType.REF,
    Regex("^return") to TokenType.RETURN,
    Regex("^self") to TokenType.SELF,
    Regex("^Self") to TokenType.SELF_CAP,
    Regex("^static") to TokenType.STATIC,
    Regex("^struct") to TokenType.STRUCT,
    Regex("^super") to TokenType.SUPER,
    Regex("^trait") to TokenType.TRAIT,
    Regex("^true") to TokenType.TRUE,
    Regex("^type") to TokenType.TYPE,
    Regex("^unsafe") to TokenType.UNSAFE,
    Regex("^use") to TokenType.USE,
    Regex("^where") to TokenType.WHERE,
    Regex("^while") to TokenType.WHILE,
    Regex("^await") to TokenType.AWAIT,
    Regex("^dyn") to TokenType.DYN,
    Regex("^abstract") to TokenType.ABSTRACT,
    Regex("^become") to TokenType.BECOME,
    Regex("^box") to TokenType.BOX,
    Regex("^do") to TokenType.DO,
    Regex("^final") to TokenType.FINAL,
    Regex("^macro") to TokenType.MACRO,
    Regex("^override") to TokenType.OVERRIDE,
    Regex("^priv") to TokenType.PRIV,
    Regex("^typeof") to TokenType.TYPEOF,
    Regex("^unsized") to TokenType.UNSIZED,
    Regex("^virtual") to TokenType.VIRTUAL,
    Regex("^yield") to TokenType.YIELD,
    Regex("^try") to TokenType.TRY,
    Regex("^gen") to TokenType.GEN,

    Regex("^[a-zA-Z][a-zA-Z0-9_]*") to TokenType.IDENTIFIER,
    Regex(
        """^(?:>>=|<<=|\+=|-=|\*=|/=|%=|\^=|&=|\|=|==|!=|<=|>=|
        |<<|>>|&&|\|\||=|\+|-|\*|/|%|\^|!|~|&|\||<|>)""".trimMargin()
    ) to TokenType.OPERATOR,
    Regex("""^(?:\.\.\.|\.{2}=|\.\.|\.|::|->|<-|=>)""") to TokenType.RANGE_PATH,
    Regex("""^[,;:?@#_$]""") to TokenType.CONTROL,
    Regex(
        """^[{}\[\]()]"""
    ) to TokenType.DELIMITER,
    Regex(
        """^(?:[0-9](?:[0-9_]*[0-9])?|0b[01](?:[01_]*[01])?|0o[0-7]
        |(?:[0-7_]*[0-7])?|0x[0-9a-fA-F](?:[0-9a-fA-F_]*[0-9a-fA-F])?)
        |(?:[iu](?:32|size))?$""".trimMargin()
    ) to TokenType.INTEGER_LITERAL,
    Regex(
        """^'(?:[^'\\\n\r\t]|\\['"\\nrt0]|\\x[0-7][0-9a-fA-F]|\\u\{[0-9a-fA-F_]{1,6}})'
        |(?:[iu](?:8|16|32|64|128|size))?$""".trimMargin()
    ) to TokenType.CHAR_LITERAL,
    Regex(
        """^"(?:[^"\\\r\n]|\\[nrt0\\"]|\\x[0-7][0-9a-fA-F]|\\u\{[0-9a-fA-F_]{1,6}}|\\\n)*"
        |(?:[iu](8|16|32|64|128|size))?$""".trimMargin()
    ) to TokenType.STRING_LITERAL,
)


data class Token(val type: TokenType, val value: String) {
    fun printToken() {
        print("type = $type , value = $value")
    }
}
