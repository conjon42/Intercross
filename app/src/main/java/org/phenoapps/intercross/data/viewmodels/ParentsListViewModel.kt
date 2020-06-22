package org.phenoapps.intercross.data.viewmodels

import org.phenoapps.intercross.data.ParentsRepository
import org.phenoapps.intercross.data.models.Parent

class ParentsListViewModel(private val repo: ParentsRepository): BaseViewModel<Parent>(repo) {

    val parents = repo.selectAll()

    val males = repo.selectAll(1)

    val females = repo.selectAll(0)

}
