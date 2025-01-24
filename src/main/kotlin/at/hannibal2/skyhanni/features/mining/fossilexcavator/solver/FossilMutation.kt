package at.hannibal2.skyhanni.features.mining.fossilexcavator.solver

enum class FossilMutation(val modification: (FossilShape) -> FossilShape) {
    ROTATE_0({ positions -> positions.rotate(0) }),
    ROTATE_90({ positions -> positions.rotate(90) }),
    ROTATE_180({ positions -> positions.rotate(180) }),
    ROTATE_270({ positions -> positions.rotate(270) }),
    FLIP_ROTATE_0({ positions -> positions.rotate(0).flipShape() }),
    FLIP_ROTATE_90({ positions -> positions.rotate(90).flipShape() }),
    FLIP_ROTATE_180({ positions -> positions.rotate(180).flipShape() }),
    FLIP_ROTATE_270({ positions -> positions.rotate(270).flipShape() });

    companion object {
        val onlyRotation = listOf(
            ROTATE_0,
            ROTATE_90,
            ROTATE_180,
            ROTATE_270
        )
    }
}
