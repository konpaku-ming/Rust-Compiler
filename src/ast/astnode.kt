package ast

enum class NodeType {
    PathInExpr, TypePath,
    ReferenceType, ArrayType, SliceType, InferredType,
    IntLiteralExpr, CharLiteralExpr, StringLiteralExpr, BooleanLiteralExpr,
    CStringLiteralExpr, RawStringLiteralExpr, RawCStringLiteralExpr,
    GroupedExpr, BlockExpr, ConstBlockExpr, BorrowExpr, DerefExpr,
    NegationExpr, BinaryExpr, ComparisonExpr, LazyBooleanExpr, TypeCastExpr,
    AssignExpr, CompoundAssignExpr,
    ArrayList, ArrayLength, IndexExpr,
    StructExpr, CallExpr, MethodCallExpr, FieldExpr,
    InfiniteLoopExpr, PredicateLoopExpr, BreakExpr, ContinueExpr, IfExpr,
    ReturnExpr, UnderscoreExpr, MatchExpr,
    LiteralPattern, IdentifierPattern, WildcardPattern,
    ReferencePattern, PathPattern,
}

sealed class ASTNode {
    abstract val type: NodeType //类型
}

sealed class StmtNode : ASTNode()

sealed class ExprNode : ASTNode()
sealed class ExprWithoutBlockNode : ExprNode()
sealed class ExprWithBlockNode : ExprNode()

sealed class ItemNode : ASTNode()

//TODO: more ItemNode

data class PathSegment(
    val identSegment: Token,
)

data class PathInExprNode(
    val path: List<PathSegment>,
) : PathExprNode() {
    override val type: NodeType = NodeType.PathInExpr
}

data class TypePathNode(
    val path: PathSegment,
) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.TypePath
}

sealed class TypeNode : ASTNode()

sealed class TypeNoBoundsNode : TypeNode()

data class ReferenceTypeNode(
    val isMut: Boolean,
    val tar: TypeNoBoundsNode
) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.ReferenceType
}

data class ArrayTypeNode(
    val elementType: TypeNode,
    val length: ExprNode
) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.ArrayType
}

data class SliceTypeNode(
    val elementType: TypeNode
) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.SliceType
}

data class InferredTypeNode(val token: Token) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.InferredType
}

sealed class LiteralExprNode() : ExprWithoutBlockNode()

data class IntLiteralExprNode(
    val raw: String
) : LiteralExprNode() {
    override val type: NodeType = NodeType.IntLiteralExpr
}

data class CharLiteralExprNode(
    val raw: String
) : LiteralExprNode() {
    override val type: NodeType = NodeType.CharLiteralExpr
}

data class StringLiteralExprNode(
    val raw: String
) : LiteralExprNode() {
    override val type: NodeType = NodeType.StringLiteralExpr
}

data class BooleanLiteralExprNode(
    val raw: String
) : LiteralExprNode() {
    override val type: NodeType = NodeType.BooleanLiteralExpr
}

data class CStringLiteralExprNode(
    val raw: String
) : LiteralExprNode() {
    override val type: NodeType = NodeType.CStringLiteralExpr
}

data class RawStringLiteralExprNode(
    val raw: String
) : LiteralExprNode() {
    override val type: NodeType = NodeType.RawStringLiteralExpr
}

data class RawCStringLiteralExprNode(
    val raw: String
) : LiteralExprNode() {
    override val type: NodeType = NodeType.RawCStringLiteralExpr
}

sealed class PathExprNode : ExprWithoutBlockNode()

data class BlockExprNode(
    val statements: List<StmtNode>, //may be empty
    val tailExpr: ExprNode?
) : ExprWithBlockNode() {
    override val type: NodeType = NodeType.BlockExpr
}

data class ConstBlockExprNode(
    val block: BlockExprNode,
) : ExprWithBlockNode() {
    override val type: NodeType = NodeType.ConstBlockExpr
}

sealed class OperatorExprNode : ExprWithoutBlockNode()

data class BorrowExprNode(
    val isMut: Boolean,
    val expr: ExprNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.BorrowExpr
}

data class DerefExprNode(
    val expr: ExprNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.DerefExpr
}

data class NegationExprNode(
    val operator: Token,
    val expr: ExprNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.NegationExpr
}

data class BinaryExprNode(
    // arithmetic or logical
    val left: ExprNode,
    val operator: Token,
    val right: ExprNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.BinaryExpr
}

data class ComparisonExprNode(
    val left: ExprNode,
    val op: Token,
    val right: ExprNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.ComparisonExpr
}

data class LazyBooleanExprNode(
    val left: ExprNode,
    val op: Token,
    val right: ExprNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.LazyBooleanExpr
}

data class TypeCastExprNode(
    val expr: ExprNode,
    val targetType: TypeNoBoundsNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.TypeCastExpr
}

data class AssignExprNode(
    val left: ExprNode,
    val right: ExprNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.AssignExpr
}

data class CompoundAssignExprNode(
    val left: ExprNode,
    val op: Token,
    val right: ExprNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.CompoundAssignExpr
}

data class GroupedExprNode(val expr: ExprNode) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.GroupedExpr
}

sealed class ArrayExprNode : ExprWithoutBlockNode()

data class ArrayListExprNode(
    val element: List<ExprNode>
) : ArrayExprNode() {
    override val type: NodeType = NodeType.ArrayList
}

data class ArrayLengthExprNode(
    val element: ExprNode,
    val length: ExprNode
) : ArrayExprNode() {
    override val type: NodeType = NodeType.ArrayLength
}

data class IndexExprNode(
    val base: ExprNode,
    val index: ExprNode
) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.IndexExpr
}

data class StructExprField(
    val name: Token, // must be identifier
    val expr: ExprNode?,
)

data class StructExprNode(
    val path: PathExprNode,
    val fields: List<StructExprField>,
) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.StructExpr
}

data class CallExprNode(
    val expr: ExprNode,
    val params: List<ExprNode>
) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.CallExpr
}

data class MethodCallExprNode(
    val expr: ExprNode,
    val method: PathSegment,
    val params: List<ExprNode>
) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.MethodCallExpr
}

data class FieldExprNode(
    val expr: ExprNode,
    val field: Token // must be identifier
) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.FieldExpr
}

sealed class Condition

data class Expression(
    val expr: ExprNode
) : Condition()

data class LetChain(
    val chain: List<LetChainCondition>
) : Condition()

data class LetChainCondition(
    val pattern: PatternNode?,
    val expr: ExprNode
)

enum class ElseType {
    NULL, BLOCK, IF
}

data class IfExprNode(
    val condition: Condition,
    val thenBranch: BlockExprNode,
    val elseType: ElseType,
    val elseBranch: ExprNode?
) : ExprWithBlockNode() {
    override val type: NodeType = NodeType.IfExpr
}

sealed class LoopExprNode : ExprWithBlockNode()

data class InfiniteLoopExprNode(
    val block: BlockExprNode
) : LoopExprNode() {
    override val type: NodeType = NodeType.InfiniteLoopExpr
}

data class PredicateLoopExprNode(
    val condition: Condition,
    val block: BlockExprNode
) : LoopExprNode() {
    override val type: NodeType = NodeType.PredicateLoopExpr
}

data class BreakExprNode(val value: ExprNode?) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.BreakExpr
}

data class ContinueExprNode(val token: Token) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.ContinueExpr
}

data class ReturnExprNode(val value: ExprNode?) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.ReturnExpr
}

data class UnderscoreExprNode(val token: Token) : ExprWithoutBlockNode() {
    override val type: NodeType = NodeType.UnderscoreExpr
}

data class MatchExprNode(
    val scrutinee: ExprNode,
    val arms: List<MatchArm>
) : ExprWithBlockNode() {
    override val type: NodeType = NodeType.MatchExpr
}

data class MatchArm(
    val pattern: PatternNode,
    val guard: ExprNode?,
    val expr: ExprNode
)

sealed class PatternNode : ASTNode()

data class LiteralPatternNode(
    val expr: LiteralExprNode,
    val negate: Boolean
) : PatternNode() {
    override val type: NodeType = NodeType.LiteralPattern
}

data class IdentifierPatternNode(
    val name: Token,
    val isRef: Boolean,
    val isMut: Boolean,
    val pattern: PatternNode? = null
) : PatternNode() {
    override val type: NodeType = NodeType.IdentifierPattern
}

data class WildcardPatternNode(val token: Token) : PatternNode() {
    override val type: NodeType = NodeType.WildcardPattern
}

data class ReferencePatternNode(
    val isMut: Boolean,
    val pattern: PatternNode
) : PatternNode() {
    override val type: NodeType = NodeType.ReferencePattern
}

data class PathPatternNode(
    val path: PathInExprNode
) : PatternNode() {
    override val type: NodeType = NodeType.PathPattern
}

