package ast

fun stringToInt(str: String): Int {
    var tempStr = str.replace("_", "")
    var base = 10
    if (tempStr.startsWith("0b")) {
        base = 2
        tempStr = tempStr.substring(2)
    } else if (tempStr.startsWith("0o")) {
        base = 8
        tempStr = tempStr.substring(2)
    } else if (tempStr.startsWith("0x")) {
        base = 16
        tempStr = tempStr.substring(2)
    }
    return tempStr.toInt(base)
}

fun stringToChar(str: String): Char {
    val tempStr = str.substring(1, str.length - 1)
    return if (tempStr.length == 1) tempStr[0]
    else {
        if (tempStr.startsWith("\\n")) '\n'
        else if (tempStr.startsWith("\\r")) '\r'
        else if (tempStr.startsWith("\\t")) '\t'
        else if (tempStr.startsWith("\\'")) '\''
        else if (tempStr.startsWith("\\\"")) '\"'
        else if (tempStr.startsWith("\\\\")) '\\'
        else if (tempStr.startsWith("\\0")) '\u0000'
        else if (tempStr.startsWith("\\x"))
            tempStr.substring(2).toInt(16).toChar()
        else error("invalid char '$str'")
    }
}

fun stringToString(str: String): String {
    val tempStr = str.substring(1, str.length - 1)
    val builder = StringBuilder()
    var i = 0
    while (i < tempStr.length) {
        val c = tempStr[i]
        i++
        if (c != '\\') {
            builder.append(c)
        } else {
            when (tempStr[i]) {
                'n' -> builder.append('\n')
                'r' -> builder.append('\r')
                't' -> builder.append('\t')
                '"' -> builder.append('"')
                '\'' -> builder.append('\'')
                '\\' -> builder.append('\\')
                '0' -> builder.append('\u0000')
                'x' -> {
                    builder.append(tempStr.substring(i + 1, i + 3).toInt(16).toChar())
                    i += 2
                }

                else -> error("invalid escape")
            }
            i++
        }
    }
    return builder.toString()
}

fun getPrecedence(tokenType: TokenType): Int {
    return when (tokenType) {
        TokenType.DoubleColon -> 15
        TokenType.Dot -> 14
        TokenType.LeftParen,
        TokenType.LeftBracket -> 13
        // - ! & && as prefix -> 12
        TokenType.AS -> 11
        TokenType.Mul,
        TokenType.Div,
        TokenType.Mod -> 10

        TokenType.Add,
        TokenType.SubNegate -> 9

        TokenType.Shl,
        TokenType.Shr -> 8

        TokenType.BitAnd -> 7
        TokenType.BitXor -> 6
        TokenType.BitOr -> 5
        TokenType.Eq,
        TokenType.Neq,
        TokenType.Lt,
        TokenType.Gt,
        TokenType.Le,
        TokenType.Ge -> 4

        TokenType.And -> 3
        TokenType.Or -> 2
        TokenType.Assign,
        TokenType.AddAssign,
        TokenType.SubAssign,
        TokenType.MulAssign,
        TokenType.DivAssign,
        TokenType.ModAssign,
        TokenType.ShlAssign,
        TokenType.ShrAssign,
        TokenType.AndAssign,
        TokenType.OrAssign,
        TokenType.XorAssign -> 1

        else -> 0
    }
}


class Parser(private val tokens: List<Token>) {
    private var position = 0
    private val tokenNum = tokens.size

    private fun peek(): Token {
        if (position >= tokenNum) error("expect token but found none")
        else return tokens[position]
    }

    private fun consume(): Token {
        if (position >= tokenNum) error("expect token but found none")
        else return tokens[position++]
    }

    private fun match(type: TokenType, value: String? = null): Boolean {
        if (position >= tokenNum) error("expect token but found none")
        val cur = tokens[position]
        if (type == cur.type && (value == null || value == cur.value)) {
            position++
            return true
        } else return false
    }

    fun isExpr(): Boolean {
        return when (peek().type) {
            TokenType.INTEGER_LITERAL,
            TokenType.CHAR_LITERAL,
            TokenType.STRING_LITERAL,
            TokenType.TRUE,
            TokenType.FALSE,
            TokenType.C_STRING_LITERAL,
            TokenType.RAW_STRING_LITERAL,
            TokenType.RAW_C_STRING_LITERAL,
            TokenType.IDENTIFIER,
            TokenType.SELF,
            TokenType.SELF_CAP,
            TokenType.LeftParen,
            TokenType.LeftBracket,
            TokenType.LeftBrace,
            TokenType.CONST,
            TokenType.Not,
            TokenType.SubNegate,
            TokenType.BitAnd,
            TokenType.And,
            TokenType.Mul,
            TokenType.IF,
            TokenType.LOOP,
            TokenType.WHILE,
            TokenType.BREAK,
            TokenType.CONTINUE,
            TokenType.RETURN,
            TokenType.Underscore,
            TokenType.MATCH -> true

            else -> false
        }
    }

    fun parseExpr(precedence: Int): ExprNode {
        var left = parsePrefix()
        while (true) {
            val cur = peek()
            val curPrecedence = getPrecedence(cur.type)
            if (curPrecedence <= precedence) break
            left = parseInfix(left)
        }
        return left
    }

    fun parsePrefix(): ExprNode {
        val token = peek()
        // TODO
        return when (token.type) {
            TokenType.INTEGER_LITERAL,
            TokenType.CHAR_LITERAL,
            TokenType.STRING_LITERAL,
            TokenType.TRUE,
            TokenType.FALSE,
            TokenType.C_STRING_LITERAL,
            TokenType.RAW_STRING_LITERAL,
            TokenType.RAW_C_STRING_LITERAL -> parseLiteralExpr(consume())

            TokenType.IDENTIFIER,
            TokenType.SELF,
            TokenType.SELF_CAP -> {
                val path = parsePathExpr()
                if (peek().type == TokenType.LeftBrace) parseStructExpr(path)
                else path
            }

            TokenType.LeftBrace -> parseBlockExpr(consume())
            TokenType.CONST -> parseConstBlockExpr(consume())
            TokenType.SubNegate -> parsePrefixNegate(consume())
            TokenType.Not -> parsePrefixNot(consume())
            TokenType.BitAnd -> parsePrefixBorrow(consume())
            TokenType.And -> parsePrefixAnd(consume())
            TokenType.Mul -> parsePrefixDeref(consume())
            TokenType.LeftParen -> parseGroupedExpr(consume())
            TokenType.LeftBracket -> parseArrayExpr(consume())
            TokenType.IF -> parseIfExpr(consume())
            TokenType.LOOP -> parseInfiniteLoopExpr(consume())
            TokenType.WHILE -> parsePredicateLoopExpr(consume())
            TokenType.BREAK -> parseBreakExpr(consume())
            TokenType.CONTINUE -> parseContinueExpr(consume())
            TokenType.RETURN -> parseReturnExpr(consume())
            TokenType.Underscore -> parseUnderscoreExpr(consume())
            TokenType.MATCH -> parseMatchExpr(consume())

            else -> error("unexpected prefix token")
        }
    }

    fun parseInfix(left: ExprNode): ExprNode {
        val token = peek()
        // TODO
        return when (token.type) {
            TokenType.Add -> parseInfixAdd(left, consume())
            TokenType.SubNegate -> parseInfixSub(left, consume())
            TokenType.Mul -> parseInfixMul(left, consume())
            TokenType.Div -> parseInfixDiv(left, consume())
            TokenType.Mod -> parseInfixMod(left, consume())
            TokenType.Shl -> parseInfixShl(left, consume())
            TokenType.Shr -> parseInfixShr(left, consume())
            TokenType.BitAnd -> parseInfixBitAnd(left, consume())
            TokenType.BitOr -> parseInfixBitOr(left, consume())
            TokenType.BitXor -> parseInfixBitXor(left, consume())
            TokenType.Lt -> parseInfixLt(left, consume())
            TokenType.Gt -> parseInfixGt(left, consume())
            TokenType.Le -> parseInfixLe(left, consume())
            TokenType.Ge -> parseInfixGe(left, consume())
            TokenType.Eq -> parseInfixEq(left, consume())
            TokenType.Neq -> parseInfixNeq(left, consume())
            TokenType.And -> parseInfixAnd(left, consume())
            TokenType.Or -> parseInfixOr(left, consume())
            TokenType.AS -> parseInfixAs(left, consume())
            TokenType.Assign -> parseInfixAssign(left, consume())
            TokenType.AddAssign -> parseInfixAddAssign(left, consume())
            TokenType.SubAssign -> parseInfixSubAssign(left, consume())
            TokenType.MulAssign -> parseInfixMulAssign(left, consume())
            TokenType.DivAssign -> parseInfixDivAssign(left, consume())
            TokenType.ModAssign -> parseInfixModAssign(left, consume())
            TokenType.ShlAssign -> parseInfixShlAssign(left, consume())
            TokenType.ShrAssign -> parseInfixShrAssign(left, consume())
            TokenType.AndAssign -> parseInfixAndAssign(left, consume())
            TokenType.OrAssign -> parseInfixOrAssign(left, consume())
            TokenType.XorAssign -> parseInfixXorAssign(left, consume())

            TokenType.LeftBracket -> parseIndexExpr(left, consume())
            TokenType.LeftParen -> parseCallExpr(left, consume())

            TokenType.Dot -> {
                consume()
                val next = peek()
                val method = parsePathSegment()
                if (peek().type == TokenType.LeftParen) {
                    parseMethodCallExpr(left, method)
                } else {
                    parseFieldExpr(left, next)
                }
            }

            else -> error("unexpected infix token")
        }
    }

    fun parseStmt(): StmtNode {
    }

    fun parsePathInExpr(): PathInExprNode {
        val path = mutableListOf<PathSegment>()
        path.add(parsePathSegment())
        while (match(TokenType.DoubleColon)) {
            path.add(parsePathSegment())
        }
        return PathInExprNode(path)
    }


    fun parsePathSegment(): PathSegment {
        val identSegment = consume()
        if (identSegment.type != TokenType.IDENTIFIER &&
            identSegment.type != TokenType.SELF &&
            identSegment.type != TokenType.SELF_CAP
        ) error("unexpected token in PathExprSegment")
        return PathSegment(identSegment)
    }

    fun parseTypePath(): TypePathNode {
        val path = parsePathSegment()
        return TypePathNode(path)
    }

    fun parseType(): TypeNode {
        return parseTypeNoBounds()
    }

    fun parseTypeNoBounds(): TypeNoBoundsNode {
        if (peek().type == TokenType.IDENTIFIER ||
            peek().type == TokenType.SELF ||
            peek().type == TokenType.SELF_CAP
        ) return parseTypePath()

        val cur = consume()
        return when (cur.type) {
            TokenType.BitAnd -> parseReferenceType(cur)
            TokenType.LeftBracket -> parseArrayOrSliceType(cur)
            TokenType.Underscore -> parseInferredType(cur)
            else -> error("unexpected token in TypeNoBounds")
        }
    }

    fun parseReferenceType(cur: Token): ReferenceTypeNode {
        if (cur.type != TokenType.BitAnd) error("expected &")
        val isMut = match(TokenType.MUT)
        val tar = parseTypeNoBounds()
        return ReferenceTypeNode(isMut, tar)
    }

    fun parseArrayOrSliceType(cur: Token): TypeNoBoundsNode {
        if (cur.type != TokenType.LeftBracket) error("expected [")
        val elementType = parseType()
        val next = consume()
        when (next.type) {
            TokenType.RightBracket -> return SliceTypeNode(elementType)
            TokenType.Semicolon -> {
                val length = parseExpr(0)
                if (!match(TokenType.RightBracket)) error("expected ]")
                return ArrayTypeNode(elementType, length)
            }

            else -> error("expected ] or ;")
        }
    }

    fun parseInferredType(cur: Token): InferredTypeNode {
        if (cur.type != TokenType.Underscore) error("expected _")
        return InferredTypeNode(cur)
    }

    fun parseLiteralExpr(cur: Token): LiteralExprNode {
        when (cur.type) {
            TokenType.INTEGER_LITERAL ->
                return IntLiteralExprNode(cur.value)

            TokenType.TRUE ->
                return BooleanLiteralExprNode(cur.value)

            TokenType.FALSE ->
                return BooleanLiteralExprNode(cur.value)

            TokenType.CHAR_LITERAL ->
                return CharLiteralExprNode(cur.value)

            TokenType.STRING_LITERAL ->
                return StringLiteralExprNode(cur.value)

            TokenType.C_STRING_LITERAL ->
                return CStringLiteralExprNode(cur.value)

            TokenType.RAW_STRING_LITERAL ->
                return RawStringLiteralExprNode(cur.value)

            TokenType.RAW_C_STRING_LITERAL ->
                return RawCStringLiteralExprNode(cur.value)

            else -> error("unexpected token")
        }
    }

    fun parsePathExpr(): PathExprNode {
        return parsePathInExpr()
    }

    fun parseBlockExpr(cur: Token): BlockExprNode {
        if (cur.type != TokenType.LeftBrace) error("expected {")
        val statements = mutableListOf<StmtNode>()
        var expression: ExprNode? = null
        while (!match(TokenType.RightBrace)) {
            if (isStmt()) {
                val stmt = parseStmt()
                statements.add(stmt)
            } else {
                val expr = parseExpr(0)
                if (match(TokenType.Semicolon)) {
                    statements.add(ExprStmtNode(expr))
                } else {
                    expression = expr
                }
            }
        }
        return BlockExprNode(statements, expression)
    }

    fun parseConstBlockExpr(cur: Token): ConstBlockExprNode {
        if (cur.type != TokenType.CONST) error("expected const")
        val block = parseBlockExpr(consume())
        return ConstBlockExprNode(block)
    }


    fun parsePrefixNegate(cur: Token): NegationExprNode {
        // - as prefix
        if (cur.type != TokenType.SubNegate) error("expected -")
        val right = parseExpr(12)
        return NegationExprNode(cur, right)
    }

    fun parsePrefixNot(cur: Token): NegationExprNode {
        // ! as prefix
        if (cur.type != TokenType.Not) error("expected !")
        val right = parseExpr(12)
        return NegationExprNode(cur, right)
    }

    fun parseInfixAdd(left: ExprNode, cur: Token): BinaryExprNode {
        // + as infix
        if (cur.type != TokenType.Add) error("expected +")
        val right = parseExpr(9)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixSub(left: ExprNode, cur: Token): BinaryExprNode {
        // - as infix
        if (cur.type != TokenType.SubNegate) error("expected -")
        val right = parseExpr(9)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixMul(left: ExprNode, cur: Token): BinaryExprNode {
        // * as infix
        if (cur.type != TokenType.Mul) error("expected *")
        val right = parseExpr(10)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixDiv(left: ExprNode, cur: Token): BinaryExprNode {
        // / as infix
        if (cur.type != TokenType.Div) error("expected /")
        val right = parseExpr(10)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixMod(left: ExprNode, cur: Token): BinaryExprNode {
        // % as infix
        if (cur.type != TokenType.Mod) error("expected %")
        val right = parseExpr(10)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixShl(left: ExprNode, cur: Token): BinaryExprNode {
        // << as infix
        if (cur.type != TokenType.Shl) error("expected <<")
        val right = parseExpr(8)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixShr(left: ExprNode, cur: Token): BinaryExprNode {
        // >> as infix
        if (cur.type != TokenType.Shr) error("expected >>")
        val right = parseExpr(8)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixBitAnd(left: ExprNode, cur: Token): BinaryExprNode {
        // & as infix
        if (cur.type != TokenType.BitAnd) error("expected &")
        val right = parseExpr(7)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixBitOr(left: ExprNode, cur: Token): BinaryExprNode {
        // | as infix
        if (cur.type != TokenType.BitOr) error("expected |")
        val right = parseExpr(5)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixBitXor(left: ExprNode, cur: Token): BinaryExprNode {
        // ^ as infix
        if (cur.type != TokenType.BitXor) error("expected ^")
        val right = parseExpr(6)
        return BinaryExprNode(left, cur, right)
    }

    fun parseInfixLt(left: ExprNode, cur: Token): ComparisonExprNode {
        // < as infix
        if (cur.type != TokenType.Lt) error("expected <")
        val right = parseExpr(4)
        return ComparisonExprNode(left, cur, right)
    }

    fun parseInfixGt(left: ExprNode, cur: Token): ComparisonExprNode {
        // > as infix
        if (cur.type != TokenType.Gt) error("expected >")
        val right = parseExpr(4)
        return ComparisonExprNode(left, cur, right)
    }

    fun parseInfixLe(left: ExprNode, cur: Token): ComparisonExprNode {
        // <= as infix
        if (cur.type != TokenType.Le) error("expected <=")
        val right = parseExpr(4)
        return ComparisonExprNode(left, cur, right)
    }

    fun parseInfixGe(left: ExprNode, cur: Token): ComparisonExprNode {
        // >= as infix
        if (cur.type != TokenType.Ge) error("expected >=")
        val right = parseExpr(4)
        return ComparisonExprNode(left, cur, right)
    }

    fun parseInfixEq(left: ExprNode, cur: Token): ComparisonExprNode {
        // == as infix
        if (cur.type != TokenType.Eq) error("expected ==")
        val right = parseExpr(4)
        return ComparisonExprNode(left, cur, right)
    }

    fun parseInfixNeq(left: ExprNode, cur: Token): ComparisonExprNode {
        // != as infix
        if (cur.type != TokenType.Neq) error("expected !=")
        val right = parseExpr(4)
        return ComparisonExprNode(left, cur, right)
    }

    fun parseInfixAnd(left: ExprNode, cur: Token): LazyBooleanExprNode {
        // && as infix
        if (cur.type != TokenType.And) error("expected &&")
        val right = parseExpr(3)
        return LazyBooleanExprNode(left, cur, right)
    }

    fun parseInfixOr(left: ExprNode, cur: Token): LazyBooleanExprNode {
        // || as infix
        if (cur.type != TokenType.Or) error("expected ||")
        val right = parseExpr(2)
        return LazyBooleanExprNode(left, cur, right)
    }

    fun parseInfixAs(left: ExprNode, cur: Token): TypeCastExprNode {
        // TypeCast
        if (cur.type != TokenType.AS) error("expected as")
        val targetType = parseTypeNoBounds()
        return TypeCastExprNode(left, targetType)
    }

    fun parsePrefixBorrow(cur: Token): BorrowExprNode {
        // & as prefix
        if (cur.type != TokenType.BitAnd) error("expected &")
        val isMut = match(TokenType.MUT)
        val expr = parseExpr(12)
        return BorrowExprNode(isMut, expr)
    }

    fun parsePrefixAnd(cur: Token): BorrowExprNode {
        // && as prefix
        if (cur.type != TokenType.And) error("expected &&")
        val isMut = match(TokenType.MUT)
        val expr = parseExpr(12)
        return BorrowExprNode(isMut, BorrowExprNode(isMut, expr))
    }

    fun parsePrefixDeref(cur: Token): DerefExprNode {
        // * as prefix
        if (cur.type != TokenType.Mul) error("expected *")
        val expr = parseExpr(12)
        return DerefExprNode(expr)
    }

    fun parseInfixAssign(left: ExprNode, cur: Token): AssignExprNode {
        // = as prefix
        if (cur.type != TokenType.Assign) error("expected =")
        val right = parseExpr(1)
        return AssignExprNode(left, right)
    }

    fun parseInfixAddAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // += as infix
        if (cur.type != TokenType.AddAssign) error("expected +=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseInfixSubAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // -= as infix
        if (cur.type != TokenType.SubAssign) error("expected -=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseInfixMulAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // *= as infix
        if (cur.type != TokenType.MulAssign) error("expected *=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseInfixDivAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // /= as infix
        if (cur.type != TokenType.DivAssign) error("expected /=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseInfixModAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // %= as infix
        if (cur.type != TokenType.ModAssign) error("expected %=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseInfixShlAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // <<= as infix
        if (cur.type != TokenType.ShlAssign) error("expected <<=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseInfixShrAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // >>= as infix
        if (cur.type != TokenType.ShrAssign) error("expected >>=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseInfixAndAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // &= as infix
        if (cur.type != TokenType.AndAssign) error("expected &=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseInfixOrAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // |= as infix
        if (cur.type != TokenType.OrAssign) error("expected |=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseInfixXorAssign(left: ExprNode, cur: Token): CompoundAssignExprNode {
        // ^= as infix
        if (cur.type != TokenType.XorAssign) error("expected ^=")
        val right = parseExpr(1)
        return CompoundAssignExprNode(left, cur, right)
    }

    fun parseGroupedExpr(cur: Token): GroupedExprNode {
        if (cur.type != TokenType.LeftParen) error("expected (")
        val expr = parseExpr(0)
        if (!match(TokenType.RightParen)) error("expected )")
        return GroupedExprNode(expr)
    }

    fun parseArrayExpr(cur: Token): ArrayExprNode {
        if (cur.type != TokenType.LeftBracket) error("expected [")
        val element = mutableListOf<ExprNode>()
        if (match(TokenType.RightBracket)) {
            return ArrayListExprNode(element)
        }
        val first = parseExpr(0)
        element.add(first)
        if (match(TokenType.RightBracket)) {
            return ArrayListExprNode(element)
        }
        if (match(TokenType.Comma)) {
            if (match(TokenType.RightBracket))
                return ArrayListExprNode(element)
            else element.add(parseExpr(0))
            while (match(TokenType.Comma)) {
                if (peek().type == TokenType.RightBracket) {
                    break
                }
                element.add(parseExpr(0))
            }
            if (!match(TokenType.RightBracket)) error("expected ]")
            return ArrayListExprNode(element)
        }
        if (match(TokenType.Semicolon)) {
            val length = parseExpr(0)
            if (!match(TokenType.RightBracket)) error("expected ]")
            return ArrayLengthExprNode(first, length)
        }
        error("unexpected token in ArrayExpr")
    }

    fun parseIndexExpr(base: ExprNode, cur: Token): IndexExprNode {
        if (cur.type != TokenType.LeftBracket) error("expected [")
        val index = parseExpr(0)
        if (!match(TokenType.RightBracket)) error("expected ]")
        return IndexExprNode(base, index)
    }

    fun parseStructExpr(path: PathExprNode): StructExprNode {
        if (!match(TokenType.LeftBrace)) error("expected {")
        val fields = mutableListOf<StructExprField>()
        if (match(TokenType.RightBrace)) return StructExprNode(path, fields)
        else fields.add(parseStructExprField())
        while (match(TokenType.Comma)) {
            if (peek().type == TokenType.RightBrace) {
                break
            }
            fields.add(parseStructExprField())
        }
        if (!match(TokenType.RightBrace)) error("expected }")
        return StructExprNode(path, fields)
    }

    fun parseStructExprField(): StructExprField {
        val name = consume()
        if (name.type != TokenType.IDENTIFIER) error("expected identifier")
        var expr: ExprNode? = null
        if (match(TokenType.Colon)) {
            expr = parseExpr(0)
        }
        return StructExprField(name, expr)
    }

    fun parseCallExpr(expr: ExprNode, cur: Token): CallExprNode {
        if (cur.type != TokenType.LeftParen) error("expected (")
        val params = mutableListOf<ExprNode>()
        if (match(TokenType.RightParen)) return CallExprNode(expr, params)
        else params.add(parseExpr(0))
        while (match(TokenType.Comma)) {
            if (peek().type == TokenType.RightParen) {
                break
            }
            params.add(parseExpr(0))
        }
        if (!match(TokenType.RightParen)) error("expected )")
        return CallExprNode(expr, params)
    }

    fun parseMethodCallExpr(
        expr: ExprNode,
        method: PathSegment
    ): MethodCallExprNode {
        if (!match(TokenType.LeftParen)) error("expected (")
        val params = mutableListOf<ExprNode>()
        if (match(TokenType.RightParen)) return MethodCallExprNode(expr, method, params)
        else params.add(parseExpr(0))
        while (match(TokenType.Comma)) {
            if (peek().type == TokenType.RightParen) {
                break
            }
            params.add(parseExpr(0))
        }
        if (!match(TokenType.RightParen)) error("expected )")
        return MethodCallExprNode(expr, method, params)
    }

    fun parseFieldExpr(expr: ExprNode, id: Token): FieldExprNode {
        if (id.type != TokenType.IDENTIFIER) error("expected identifier")
        return FieldExprNode(expr, id)
    }

    fun parsePattern(): PatternNode {
        return when (peek().type) {
            TokenType.Underscore -> parseWildcardPattern()
            TokenType.And -> parseReferencePattern()
            TokenType.BitAnd -> parseReferencePattern()
            TokenType.SubNegate -> parseLiteralPattern()
            TokenType.INTEGER_LITERAL -> parseLiteralPattern()
            TokenType.CHAR_LITERAL -> parseLiteralPattern()
            TokenType.STRING_LITERAL -> parseLiteralPattern()
            TokenType.FALSE -> parseLiteralPattern()
            TokenType.TRUE -> parseLiteralPattern()
            TokenType.REF -> parseIdentifierPattern()
            TokenType.MUT -> parseIdentifierPattern()
            TokenType.SELF -> parsePathPattern()
            TokenType.SELF_CAP -> parsePathPattern()
            TokenType.IDENTIFIER -> {
                when (tokens[position + 1].type) {
                    TokenType.At -> parseIdentifierPattern()
                    TokenType.DoubleColon -> parsePathPattern()
                    else -> parseIdentifierPattern()
                }
            }

            else -> error("unexpected token in pattern")
        }
    }

    fun parseLiteralPattern(): LiteralPatternNode {
        val negate = match(TokenType.SubNegate)
        val cur = consume()
        val expr = parseLiteralExpr(cur)
        return LiteralPatternNode(expr, negate)
    }

    fun parseIdentifierPattern(): IdentifierPatternNode {
        val isRef = match(TokenType.REF)
        val isMut = match(TokenType.MUT)
        val name = consume()
        if (name.type != TokenType.IDENTIFIER) error("expected identifier")
        if (match(TokenType.At)) {
            val pattern = parsePattern()
            return IdentifierPatternNode(name, isRef, isMut, pattern)
        } else return IdentifierPatternNode(name, isRef, isMut, null)
    }

    fun parseWildcardPattern(): WildcardPatternNode {
        val underscore = consume()
        if (underscore.type != TokenType.Underscore) error("expected _")
        return WildcardPatternNode(underscore)
    }

    fun parseReferencePattern(): ReferencePatternNode {
        val ref = consume()
        if (ref.type != TokenType.And && ref.type != TokenType.BitAnd)
            error("expected & or &&")
        val isMut = match(TokenType.MUT)
        val pattern = parsePattern()
        return if (ref.type == TokenType.BitAnd)
            ReferencePatternNode(isMut, pattern)
        else ReferencePatternNode(false, ReferencePatternNode(isMut, pattern))
    }

    fun parsePathPattern(): PathPatternNode {
        val path = parsePathInExpr()
        return PathPatternNode(path)
    }

    fun parseCondition(): Condition {
        return if (peek().type == TokenType.LET) {
            parseLetChain()
        } else {
            val expr = parseExpr(0)
            if (expr is StructExprNode) {
                error("Excluded expression type in condition")
            }
            Expression(expr)
        }
    }

    fun parseLetChain(): LetChain {
        val chain = mutableListOf<LetChainCondition>()
        chain.add(parseLetChainCondition())
        while (match(TokenType.And)) {
            chain.add(parseLetChainCondition())
        }
        return LetChain(chain)
    }

    fun parseLetChainCondition(): LetChainCondition {
        return if (match(TokenType.LET)) {
            val pattern = parsePattern()
            if (!match(TokenType.Eq)) error("expected =")
            val expr = parseExpr(0)
            if (expr is StructExprNode || expr is LazyBooleanExprNode ||
                expr is AssignExprNode || expr is CompoundAssignExprNode
            ) {
                error("expr : excluded conditions")
            }
            LetChainCondition(pattern, expr)
        } else {
            val expr = parseExpr(0)
            if (expr is StructExprNode || expr is LazyBooleanExprNode ||
                expr is AssignExprNode || expr is CompoundAssignExprNode
            ) {
                error("expr : excluded conditions")
            }
            LetChainCondition(null, expr)
        }
    }

    fun parseIfExpr(cur: Token): IfExprNode {
        if (cur.type != TokenType.IF) error("expected if")
        val condition = parseCondition()
        val thenBranch = parseBlockExpr(consume())
        var elseType = ElseType.NULL
        var elseBranch: ExprNode? = null
        if (match(TokenType.ELSE)) {
            val token = consume()
            when (token.type) {
                TokenType.IF -> {
                    elseBranch = parseIfExpr(token)
                    elseType = ElseType.IF
                }

                TokenType.LeftBrace -> {
                    elseBranch = parseBlockExpr(token)
                    elseType = ElseType.BLOCK
                }

                else -> error("expected if or {")
            }
        }
        return IfExprNode(condition, thenBranch, elseType, elseBranch)
    }

    fun parseInfiniteLoopExpr(cur: Token): InfiniteLoopExprNode {
        if (cur.type == TokenType.LOOP) error("expected loop")
        val block = parseBlockExpr(consume())
        return InfiniteLoopExprNode(block)
    }

    fun parsePredicateLoopExpr(cur: Token): PredicateLoopExprNode {
        if (cur.type == TokenType.WHILE) error("expected while")
        val condition = parseCondition()
        val block = parseBlockExpr(consume())
        return PredicateLoopExprNode(condition, block)
    }

    fun parseBreakExpr(cur: Token): BreakExprNode {
        if (cur.type != TokenType.BREAK) error("expected break")
        if (isExpr()) {
            val value = parseExpr(0)
            return BreakExprNode(value)
        } else return BreakExprNode(null)
    }

    fun parseContinueExpr(cur: Token): ContinueExprNode {
        if (cur.type != TokenType.CONTINUE) error("expected continue")
        return ContinueExprNode(cur)
    }

    fun parseReturnExpr(cur: Token): ReturnExprNode {
        if (cur.type != TokenType.RETURN) error("expected return")
        if (isExpr()) {
            val value = parseExpr(0)
            return ReturnExprNode(value)
        } else return ReturnExprNode(null)
    }

    fun parseUnderscoreExpr(cur: Token): UnderscoreExprNode {
        if (cur.type != TokenType.Underscore) error("expected _")
        return UnderscoreExprNode(cur)
    }

    fun parseMatchArm(): MatchArm {
        val pattern = parsePattern()
        val guard = if (match(TokenType.IF)) {
            parseExpr(0)
        } else null
        if (!match(TokenType.FatArrow)) error("expected =>")
        val expr = parseExpr(0)
        val isComma = match(TokenType.Comma)
        return if (expr is ExprWithBlockNode) {
            MatchArm(pattern, guard, expr)
        } else {
            if (peek().type == TokenType.RightBrace) {
                MatchArm(pattern, guard, expr)// 最后一个
            } else {
                if (!isComma) error("expected ,")
                else MatchArm(pattern, guard, expr)
            }
        }
    }

    fun parseMatchExpr(cur: Token): MatchExprNode {
        if (cur.type != TokenType.MATCH) error("expected match")
        val scrutinee = parseExpr(0)
        if (scrutinee is StructExprNode) error("unexpected scrutinee")
        if (!match(TokenType.LeftBrace)) error("expected {")
        val arms = mutableListOf<MatchArm>()
        while (!match(TokenType.RightBrace)) {
            arms.add(parseMatchArm())
        }
        return MatchExprNode(scrutinee, arms)
    }
}
