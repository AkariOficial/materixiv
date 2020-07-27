package onlymash.materixiv.ui.module.common

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import onlymash.materixiv.data.api.PixivOauthApi
import onlymash.materixiv.data.db.dao.TokenDao
import onlymash.materixiv.data.db.entity.Token
import onlymash.materixiv.data.repository.NetworkState
import onlymash.materixiv.data.repository.token.TokenRepositoryImpl
import onlymash.materixiv.extensions.getViewModel
import onlymash.materixiv.ui.base.KodeinActivity
import onlymash.materixiv.ui.module.login.LoginActivity
import org.kodein.di.instance

abstract class TokenActivity : KodeinActivity() {

    private val tokenDao by instance<TokenDao>()
    private val pixivOauthApi by instance<PixivOauthApi>()

    private lateinit var tokenViewModel: TokenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onLoadTokenBefore(savedInstanceState)
        tokenViewModel = getViewModel(TokenViewModel(TokenRepositoryImpl(pixivOauthApi, tokenDao)))
        tokenViewModel.load().observe(this, Observer { tokens ->
            if (tokens.isNullOrEmpty()) {
                toLoginPage()
            } else {
                val token = tokens[0]
                if (token.isExpired) {
                    refreshToken(token.uid, token.data.refreshToken, token.data.deviceToken)
                } else {
                    onTokenLoaded(token)
                }
            }
        })
        tokenViewModel.loginState.observe(this, Observer {
            onLoginStateChange(it)
        })
        tokenViewModel.refreshState.observe(this, Observer {
            onRefreshStateChange(it)
        })
    }

    abstract fun onLoadTokenBefore(savedInstanceState: Bundle?)

    private fun toLoginPage() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    abstract fun onTokenLoaded(token: Token)

    abstract fun onLoginStateChange(state: NetworkState?)

    abstract fun onRefreshStateChange(state: NetworkState?)

    protected fun login(username: String, password: String) {
        tokenViewModel.login(username, password)
    }

    protected fun refreshToken(uid: Long, refreshToken: String, deviceToken: String) {
        tokenViewModel.refresh(uid, refreshToken, deviceToken)
    }
}