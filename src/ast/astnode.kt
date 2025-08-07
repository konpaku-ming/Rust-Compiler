package ast

enum class NodeType{

}

sealed class ASTNode {
    abstract val line: Int
    abstract val type: NodeType
}
