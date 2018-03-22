package astah.plugin.SacmForGsn

enum class SACMType {
    Claim {
        override val sacmPrefix = "G"
        override val sacmXsiType = "Claim"
    },
    ArgumentReasoning {
        override val sacmPrefix = "S"
        override val sacmXsiType = "ArgumentReasoning"
    },
    ContextInformationElement {
        override val sacmPrefix = "C"
        override val sacmXsiType = "InformationElement"
    },
    SolutionInformationElement {
        override val sacmPrefix = "Sn"
        override val sacmXsiType = "InformationElement"
    },
    AssertedContext {
        override val sacmPrefix = "_AC_"
        override val sacmXsiType = "AssertedContext"
    },
    AssertedEvidence {
        override val sacmPrefix = "_AE_"
        override val sacmXsiType = "AssertedEvidence"
    },
    AssertedInference {
        override val sacmPrefix = "_AI_"
        override val sacmXsiType = "AssertedInference"
    };

    abstract val sacmPrefix: String
    abstract val sacmXsiType: String

    companion object {
        fun isLeafType(type: SACMType): Boolean {
            return type == ContextInformationElement || type == SolutionInformationElement
        }
        fun isInternalType(type: SACMType): Boolean {
            return !isLeafType(type)
        }
        fun isArrowType(type: SACMType): Boolean {
            return type == AssertedContext || type == AssertedEvidence || type == AssertedInference
        }
    }
}