package org.phenoapps.intercross.data.viewmodels

import org.phenoapps.intercross.data.MetaValuesRepository
import org.phenoapps.intercross.data.models.MetadataValues

class MetaValuesViewModel(private val repo: MetaValuesRepository): BaseViewModel<MetadataValues>(repo) {

    val metaValues = repo.selectAll()

}
