package ast


enum class NodeType {
    SimplePath, PathInExpr, QualifiedPath, TypePath,
    ParenthesizedType, TupleType, NeverType, RawPointerType, ReferenceType,
    ArrayType, SliceType, InferredType,
    IntLiteralExpr, CharLiteralExpr, StringLiteralExpr, BooleanLiteralExpr,
    Variable, BlockExpr, BorrowExpr, DerefExpr, NegationExpr, BinaryExpr,
    ComparisonExpr, LazyBooleanExpr, TypeCastExpr, Assign, If, Declare, GroupingExpr, IndexExpr
}

sealed class ASTNode {
    abstract val type: NodeType //类型
}

sealed class StmtNode : ASTNode()

sealed class ExprNode : ASTNode()

sealed class PathNode : ASTNode()

sealed class ItemNode : ASTNode()

//TODO: more ItemNode

data class SimplePathNode(
    val path: List<Token>,
    val isAbsolute: Boolean,
) : PathNode() {
    override val type: NodeType = NodeType.SimplePath
}

class PathExprSegment(
    val identSegment: Token,
    val genericArgs: List<TypeNode>? = null
)

data class PathInExprNode(
    val path: List<PathExprSegment>,
    val isAbsolute: Boolean
) : ExprNode() {
    override val type: NodeType = NodeType.PathInExpr
}

data class QualifiedPathNode(val path: List<Token>) : PathNode() {
    override val type: NodeType = NodeType.QualifiedPath
}

data class TypePathFn(
    val inputs: List<TypeNode>,
    val output: TypeNoBoundsNode? = null
)

data class TypePathSegment(
    val identSegment: Token,
    val genericArgs: List<TypeNode>? = null,
    val fn: TypePathFn? = null
)

data class TypePathNode(
    val path: List<TypePathSegment>,
    val isAbsolute: Boolean
) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.TypePath
}

sealed class TypeNode : ASTNode()

sealed class TypeNoBoundsNode : TypeNode()

data class ParenthesizedTypeNode(val innerType: TypeNode) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.ParenthesizedType
}

data class TupleTypeNode(val elements: List<TypeNode>) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.TupleType
}

data class NeverTypeNode(val bang: String) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.NeverType
}

data class RawPointerTypeNode(
    val mutable: Boolean,
    val tar: TypeNode
) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.RawPointerType
}

data class ReferenceTypeNode(
    val mutable: Boolean,
    val tar: TypeNode
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

data class InferredTypeNode(val underScore: String) : TypeNoBoundsNode() {
    override val type: NodeType = NodeType.InferredType
}

sealed class LiteralExprNode(
) : ExprNode()

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

sealed class PathExprNode : ExprNode()

data class VariableExprNode(val path: List<String>) : PathExprNode() {
    override val type: NodeType = NodeType.Variable
}

sealed class OperatorExprNode : ExprNode()

enum class BorrowType {
    Shared, Mutable, RawConst, RawMut
}

data class BorrowExprNode(
    val borrowType: BorrowType,
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
    val targetType: TypeNode
) : OperatorExprNode() {
    override val type: NodeType = NodeType.TypeCastExpr
}


data class IndexExprNode(val base: ExprNode, val index: ExprNode) : ExprNode() {
    override val type: NodeType = NodeType.IndexExpr
}

data class BlockExprNode(
    val statements: List<StmtNode>,
    val tailExpr: ExprNode? //可能没有
) : ExprNode() {
    override val type: NodeType = NodeType.BlockExpr
}


enum class ValueType {
    UnitValue,
    BoolValue, CharValue, I32Value, ISizeValue, U32Value, USizeValue,
    StrValue, StringValue, Slice,
    Tuple, Array, Vec,
    Struct, Enum,
    Ref, MutRef, Ptr, MutPtr,
    Function,
}


data class DeclareNode(
    val name: String,
    val valueType: ValueType?,
    val initValue: ExprNode?,
    val isMutable: Boolean
) : StmtNode() {
    override val type: NodeType = NodeType.Declare
}


enum class AssignmentOp(val symbol: String) {
    Assign("="),
    AddAssign("+="),
    SubAssign("-="),
    MulAssign("*="),
    DivAssign("/="),
    ModAssign("%="),
    ShlAssign("<<="),
    ShrAssign(">>="),
    AndAssign("&="),
    OrAssign("|="),
    XorAssign("^=");
}


data class AssignNode(
    val lhs: ExprNode,
    val operator: AssignmentOp,
    val rhs: ExprNode
) : StmtNode() {
    override val type: NodeType = NodeType.Assign
}

data class IfNode(
    val condition: ExprNode,
    val thenBranch: StmtNode,
    val elseBranch: StmtNode? = null
) : StmtNode() {
    override val type: NodeType = NodeType.If
}

data class GroupingExprNode(val expr: ExprNode) : ExprNode() {
    override val type: NodeType = NodeType.GroupingExpr
}
