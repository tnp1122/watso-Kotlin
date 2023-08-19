package com.watso.app.feature.baedal.ui.view.baedalList

import com.watso.app.feature.baedal.data.BaedalPost
import java.time.LocalDate

data class Table(
    val date: LocalDate,
    val rows: MutableList<BaedalPost>
    )
