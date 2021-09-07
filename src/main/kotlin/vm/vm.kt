package vm

import objectrepr.BooleanRepr
import objectrepr.IntegerRepr
import objectrepr.ObjectRepr

data class VM(
    val bytecode: Bytecode
) {
    private var stack: MutableList<ObjectRepr> = mutableListOf()

    private fun push(objectRepr: ObjectRepr) {
        stack.add(objectRepr)
    }

    private fun pop(): ObjectRepr {
        return stack.removeLast()
    }

    private fun executeArithmeticExpression(opcode: Opcode, left: IntegerRepr, right: IntegerRepr): Int? {
        return when (opcode) {
            Opcode.Add -> left.value + right.value
            Opcode.Sub -> left.value - right.value
            Opcode.Mul -> left.value * right.value
            Opcode.Div -> left.value / right.value
            else -> null
        }
    }

    private fun executeIntegerComparison(opcode: Opcode, left: IntegerRepr, right: IntegerRepr) {
        when (opcode) {
            Opcode.Equal -> push(BooleanRepr(right.value == left.value))
            Opcode.NotEqual -> push(BooleanRepr(right.value != left.value))
            Opcode.GreaterThan -> push(BooleanRepr(left.value > right.value))
            else -> {
                // FIX exhaustiveness
            }
        }
    }

    fun run() {
        var ip = 0
        while (ip < bytecode.instructions.size) {
            when (val opcode = Opcode.values().find { it.code == bytecode.instructions[ip] })  {
                Opcode.Constant -> {
                    val (high, low) = bytecode.instructions.slice(ip + 1..ip + 2)
                    val constIndex = (high * 256u).toInt() + low.toInt()
                    ip += 2
                    push(bytecode.constants[constIndex])
                }
                Opcode.Add,
                Opcode.Sub,
                Opcode.Mul,
                Opcode.Div -> {
                    val (left, right) = Pair(pop(), pop())
                    if (left is IntegerRepr && right is IntegerRepr) {
                        val result = executeArithmeticExpression(opcode, left, right)
                        if (result != null) {
                            push(IntegerRepr(result))
                        }
                    }
                }
                Opcode.True -> push(BooleanRepr(true))
                Opcode.False -> push(BooleanRepr(false))
                Opcode.Equal,
                Opcode.NotEqual,
                Opcode.GreaterThan -> {
                    val (left, right) = Pair(pop(), pop())
                    if (left is IntegerRepr && right is IntegerRepr) {
                        executeIntegerComparison(opcode, left, right)
                    } else if (opcode == Opcode.Equal) {
                        // FIX this comparison
                        push(BooleanRepr(left == right))
                    } else if (opcode == Opcode.NotEqual) {
                        // FIX this comparison
                        push(BooleanRepr(left != right))
                    }
                }
                Opcode.Bang -> {
                    val computed = when (val operand = pop()) {
                        is BooleanRepr -> !operand.value
                        else -> false
                    }
                    push(BooleanRepr(computed))
                }
                Opcode.Minus -> {
                    val operand = pop()
                    if (operand is IntegerRepr) {
                        push(IntegerRepr(-operand.value))
                    }
//                    else {
//                     ERROR handling goes here
//                    }

                }
                Opcode.Pop -> pop()
            }
            ip++
            println(stack)
        }
    }
}


