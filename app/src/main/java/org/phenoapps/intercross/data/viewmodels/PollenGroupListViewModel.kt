package org.phenoapps.intercross.data.viewmodels

import org.phenoapps.intercross.data.PollenGroupRepository
import org.phenoapps.intercross.data.models.PollenGroup

class PollenGroupListViewModel(repo: PollenGroupRepository): BaseViewModel<PollenGroup>(repo) {

    val groups = repo.selectAll()

}
