package onlymash.materixiv.ui.module.novel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import onlymash.materixiv.data.action.ActionNovel
import onlymash.materixiv.data.model.common.Novel
import onlymash.materixiv.data.repository.novel.NovelRepository
import onlymash.materixiv.ui.base.ScopeViewModel

class NovelViewModel(private val repo: NovelRepository) : ScopeViewModel() {

    private val _clearListCh = Channel<Unit>(Channel.CONFLATED)
    private val _action = MutableLiveData<ActionNovel>()

    val novels = flowOf(
        _clearListCh.consumeAsFlow().map { PagingData.empty<Novel>() },
        _action.asFlow().flatMapLatest { repo.getNovels(it) }
    )
        .flattenMerge(2)
        .cachedIn(viewModelScope)
        .asLiveData()

    private fun shouldShow(action: ActionNovel) = _action.value != action

    fun show(action: ActionNovel) {
        if (!shouldShow(action)) return
        _clearListCh.offer(Unit)
        _action.value = action
    }
}