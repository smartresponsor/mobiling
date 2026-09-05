package app.mobiling.client.data.cart

interface CartTokenStore {
    fun current(): String?

    fun save(token: String)
}

class InMemoryCartTokenStore : CartTokenStore {
    private var token: String? = null

    override fun current(): String? = token

    override fun save(token: String) {
        this.token = token.trim().takeIf { it.isNotEmpty() }
    }
}
