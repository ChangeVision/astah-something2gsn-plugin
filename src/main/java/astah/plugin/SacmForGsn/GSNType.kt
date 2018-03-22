package astah.plugin.SacmForGsn

import java.util.*

enum class GSNType {
    Goal {
        override fun toString(): String = rb.getString("goal_id")
    },
    Strategy {
        override fun toString(): String = rb.getString("strategy_id")
    },
    Solution {
        override fun toString(): String = rb.getString("solution_id")
    },
    Context {
        override fun toString(): String = rb.getString("context_id")
    },
    Justification {
        override fun toString(): String = rb.getString("justification_id")
    },
    Assumption {
        override fun toString(): String = rb.getString("assumption_id")
    };
    abstract override fun toString() : String
    companion object {
        fun obtainType(s : String?) : GSNType? {
            return s?.let {
                when (s.toLowerCase()) {
                    Goal.toString() -> Goal
                    Strategy.toString() -> Strategy
                    Solution.toString() -> Solution
                    Context.toString() -> Context
                    Justification.toString() -> Justification
                    Assumption.toString() -> Assumption
                    else -> null
                }
            }
        }
    }
    val rb = ResourceBundle.getBundle("plugin")
    val rb_ja = ResourceBundle.getBundle("plugin_ja")
    private fun checkId(toBeChecked : String, key : String): Boolean =
            toBeChecked.equals(rb.getString(key)) || toBeChecked.equals(rb_ja.getString(key))
}