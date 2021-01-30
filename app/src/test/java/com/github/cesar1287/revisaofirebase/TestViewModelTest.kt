package com.github.cesar1287.revisaofirebase

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito

@RunWith(JUnit4::class)
class TestViewModelTest {

    private lateinit var testViewModel: TestViewModel

    @Before
    fun setup() {
        testViewModel = Mockito.spy(TestViewModel())
    }

    @Test
    fun helloWorldWithPersonalizedMessage() {
        val name = "César"
        assertEquals("Olá César!", testViewModel.helloWorld(name))
    }

    @Test
    fun helloWorldWithGenericMessage() {
        assertEquals("Olá Novo Mundo!", testViewModel.helloWorld())
    }

    @Test
    fun `Testando método hello world com mensagem personalizada`() {
        val name = "César"
        assertEquals("Olá César!", testViewModel.helloWorld(name))
    }

    @Test
    fun `Testando método hello world com mensagem padrão`() {
        assertEquals("Olá Novo Mundo!", testViewModel.helloWorld())
    }

    @Test
    fun `Testando se o último filme está sendo adicionado corretamente`() {
        val expectedList = mutableListOf(
                "Edu", "Leandro, Jonathas", "Cesar", "Luciana", "Roberto"
        )

        val classList = mutableListOf(
                "Edu", "Leandro, Jonathas", "Luciana", "Roberto"
        )

        val resultList = testViewModel.changeClassList(classList)

        assertTrue("Comparando duas listas",
                expectedList.size == resultList.size
                        && expectedList.containsAll(resultList)
                        && resultList.containsAll(expectedList))
    }

    @Test
    fun `Verificar se as entradas não são iguais`() {
        val expectedResult = "Cesar"
        val result = testViewModel.changeString(expectedResult)
        assertNotEquals(expectedResult, result)
    }

    @Test
    fun `Verificar se o retorno não é nulo`() {
        assertNotNull(testViewModel.verifyIfNull("Teste"))
    }

    @Test
    fun `Verificar se o retorno é nulo`() {
        assertNull(testViewModel.verifyIfNull())
    }

    @Test
    fun `Verifica se tem o mesmo endereço de memória`() {
        val expectedList = mutableListOf(
                "Edu", "Leandro, Jonathas", "Luciana", "Roberto"
        )

        val classList = mutableListOf(
                "Edu", "Leandro, Jonathas", "Luciana", "Roberto"
        )
        assertSame(expectedList, classList)
    }

//    @Test
//    fun `Teste condicional se uma regra se encaixa no parâmetro`(){
//        assertThat("Verificando o regex do email",
//            "cnascimento@digitalhouse.com",
//            BaseMatcher<"/^[a-z0-9.]+@[a-z0-9]+\\.[a-z]+\\.([a-z]+)?\$/i".toRegex()>
//        )
//    }
}