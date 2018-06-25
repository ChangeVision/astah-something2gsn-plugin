package astah.plugin.SacmForGsn

import java.util.*

enum class GSNType {
    Goal {
        override fun toString(): String = rb.getString("goal_id")
        override fun toJpString(): String = rb.getString("goal_jp_id")
    },
    Strategy {
        override fun toString(): String = rb.getString("strategy_id")
        override fun toJpString(): String = rb.getString("strategy_jp_id")
    },
    Solution {
        override fun toString(): String = rb.getString("solution_id")
        override fun toJpString(): String = rb.getString("solution_jp_id")
    },
    Context {
        override fun toString(): String = rb.getString("context_id")
        override fun toJpString(): String = rb.getString("context_jp_id")
    },
    Justification {
        override fun toString(): String = rb.getString("justification_id")
        override fun toJpString(): String = rb.getString("justification_jp_id")
    },
    Assumption {
        override fun toString(): String = rb.getString("assumption_id")
        override fun toJpString(): String = rb.getString("assumption_jp_id")
    };
    abstract override fun toString() : String
    abstract fun toJpString() : String
    companion object {
        val rb = ResourceBundle.getBundle("plugin")
        fun obtainType(s : String?) : GSNType? {
            return s?.let {
                when (s.toLowerCase()) {
                    Goal.toString(), Goal.toJpString() -> Goal
                    Strategy.toString(), Strategy.toJpString() -> Strategy
                    Solution.toString(), Solution.toJpString() -> Solution
                    Context.toString(), Context.toJpString() -> Context
                    Justification.toString(), Justification.toJpString() -> Justification
                    Assumption.toString(), Assumption.toJpString() -> Assumption
                    else -> null
                }
            }
        }
    }
}