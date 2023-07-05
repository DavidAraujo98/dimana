# Tema **DimAna**, grupo **dimana-02**

---

## Constituição dos grupos e participação individual global

|  NMec  | Nome                                  | email                    | Participação |
| :----: | :------------------------------------ | :----------------------- | :------------: |
| 93444 | DAVID JOSÉ ARAÚJO FERREIRA          | davidaraujo@ua.pt        |      18%      |
| 102648 | EDUARDO LOPES FERNANDES               | edu.fernandes@ua.pt      |      19%      |
| 103244 | GONÇALO LOURENÇO DA SILVA           | goncalolsilva@ua.pt      |      17%      |
| 103037 | JOÃO AFONSO PEREIRA FERREIRA         | ferreiraafonsojoao@ua.pt |      11%      |
| 103173 | PEDRO DURVAL CABRAL POLÓNIA CRUZEIRO | pedrocruzeiro@ua.pt      |      13%      |
| 103554 | TIAGO ANDRÉ LOURENÇO SILVESTRE      | tiago.silvestre@ua.pt    |      22%      |

## Estrutura do repositório

- **src** - deve conter todo o código fonte do projeto.
- **doc** -- deve conter toda a documentação adicional a este README.
- **examples** -- deve conter os exemplos ilustrativos das linguagens criadas.

  - Estes exemplos devem conter comentários (no formato aceite pelas linguagens),
    que os tornem auto-explicativos.

## Relatório

- Use esta secção para fazer um relatório sucinto mas explicativo dos objetivos concretizados.

## Gramática

### Estruturas

Na produção da gramática, foram identificadas **6 estruturas** principais que compõem a linguagem *dimana*.

1. `assig` - Atribuições, compreendem todas as estruturas que possuam um sinal de `=` entre duas outras unidades.
2. `decl` - Declarações, utilizadas para declarar variveis ou novos tipos de `dimension`.
3. `write` - Escrita para *stdout*, terminada, ou não, com um *newline*.
4. `appnd` - *Append*, utilizadas por expressões de forma a redirecionar o seu retorno para o fim de uma lista.
5. `loop` - Ciclos, utilizam `expr` com retorno *booleano* para verificar o estado da iteração e podem conter qualquer tipo de estrutura ja menscionada no seu interior.
6. `cond` - Condicionais, similares a `loop` e aceitam também a possibilidade de encadeamento de condicionais.

### Tipos

A gramatica compreende **5 tipos** de dados diferentes:

- `list[type]` - Converte qualquer outro tipo de dado numa lista de elementos desse mesmo tipo.
- `numerictype` - Qualquer valor número inteiro ou decimal, sendo que decimais poderão ser representados em notação científica.
- `string` - Qualquer conteúdo que seja delimitado por `"`.
- `bool` - Booleanos, suportando duas palavras reservadas `true` e `false`.
- `ID` - Utilizado para referenciar tipo de dimensão criado pelo utilizador.

### Declarações

Declarações podem ser contruidas com qualquer um dos tipos referenciados anteriormente em relação a uma variavel desejada, ou podem ser utilizados para definir um novo tipo de dimensão. Dimensões poderão ser apenas do `numerictype` e carecem de um identificador para o seu sufixo e unidade (unidade é opcional). A nível interno, esta estrutura devolverá o nome a dimensão criada para poder ser utilziada no Analisador Semântico e Compilador.

### Atribuições

Atribuições podem suprimir declarações, ou podem ser utilizadas por variaveis já declaradas. Podem também ser utilizadas para iniciar unidades *non SI* que sejam dependentes de outras dimensões.
Finalmente, podem também ser utilizadas para definir prefixos para variavies do tipo numérico.

### Condicionais

Condicionais aceitam qualquer tipo de expressão de verificação desde que retornem um valor booleano ou available como operação booleana. Permitem qualquer tipo de estrutura no seu interior podendo ser encadeadas através da palavra reservada `elif`, utilizada para verificações sequenciais. Poderão ou não no final conter no máximo um `else` que também permitirá qualquer tipo de estrutura no seu interior.

### Expressões

As estruturas mais comuns da gramática são expressões, relacionadas principalmente com operações que de alguma forma retornaram um valor. Compreendem as seguintes operações:

- Criação de *strings* com tamanho de *buffer* definido.
- Conversão de expressões para valores de texto.
- Conversão de expressões para valores numericos.
- Cálculo de tamanho de uma expressão.
- Operação de leitura de `stdin`
- Operação de inicialização de novo tipo com keyword `new`.
- Operações de `casting` dado o identificador de uma unidade.
- Expressões unárias.
- Operações aritméticas de soma, subtração, multiplicação, divisão e resto.
- Operações de comparação booleanas como igualdade, diferença, maior, menor, maior ou igual e menor ou igual.
- Operação de indexação de um elemento de uma lista.
- Expressões de valor com sufixo.
- Expressões de tipo, booleano, string, numerico ou variável.
- Expressões condicionais (conjunção e disjunção) em `loops`, `if` e variáveis

### Expressões Terminais

Aqui indicamos como são definidos cada um dos tipo:

- `STRING` - Qualquer sequencia de caracters, incluindo `"`, que se encontrem delimitados por um par de `"`
- `ID` - Qualquer valor alfanumérico que não seja iniciado por um digito.
- `BOOLEAN` - palavras reservadas `true` ou `false`.
- `INTEGER` - Qualquer valor numérico, se possuindo mais que um digito não pode ser começado por `0`
- `REAL` - Qualquer valor numerico do tipo `INTEGER` com um `.` que indique a sua parte decimal, ou com `e` seguido de `INTEGER` que quantifique as casas decimais.
- `COMMENT` - Comentarios, qualquer sequência começada por `#` e que será depois ignorada.

## Imports

### Abordagens consideradas

Foram consideradas 3 abordagens para implementação de imports no programa. Duas delas focavam-se em criar abstratamente uma gramática, que atuava entre o lexer e o Parser ou no Parser e que permitiam analisar e processar ficheiros importavados antes da criação da árvore sintática. As abordagens podem ser analisadas com maior detalhe no ficheiro [Implementações de Imports consideradas](./doc/Implementacao_imports.pdf)

### Abordagem escolhida

A abordagem escolhida a primeira considerada no ficheiro, ter um pré-compilador que só visita os `Use` e guarda o nome do ficheiro importado. De seguida, uma função percorre a lista de ficheiros importados e gera uma árvore sintática para cada um, guardando numa mapa o nome do ficheiro e a árvore gerada.

Tanto na análise sintática como no compilador, quando estes entram entram no `Use`, vão procurar no mapa o nome do ficheiro e depois visitar a respetiva árvore.

### Proteção contra imports circulares

Para proteger contra imports circulares, uma lista é criada para guadar todos os ficheiros já processados. Quando é encontrado um ficheiro que já foi processado, este é simplesmente ignorado. Este comportamento é implementado tanto na função de processamento de imports, como no analisador semântico e compilador.

### Aplicação em deteção de erros semânticos

A inclusão de imports significa que o código não estará todo numa localização. Daí o analisador semântico possui uma função que indica a linha de código onde um erro está presente, bem como ficheiro correspondente. Da parte dos ficheiros, isto é feito com uma lista. Sempre que a árvore semântica de um ficheiro é processado, o seu nome é posto em uma lista. Se nenhum erro ocorrer, então este será removido da lista. Caso aconteça, o ficheiro associado ao erro será o último na lista.

### Imports relativos

Visto que muitas vezes quando estamos a desenvolver estamos a trabalhar num diretório, mas a compilar noutro, o nosso compilador tem de conseguir lidar com imports relativos, ou seja, ao compilar um ficheiro localizado noutro diretório e os ficheiros importados por ele serem referentes ao seu diretório e não ao do compilador ou atual onde nos encontramos. A solução foi guardar a localização do diretório que o utilizador informa quando inicia o programa com o ficheiro para compilar e quando vamos ler os ficheiros para construir a árvore sintática, acrescentamos a todos os ficheiros o diretório do ficheiro principal.

## Análise semântica

### Funcionamento geral

Foi utilizado um Visitor como o objetivo de fazer a análise semântica do código fonte, cada função retorna sempre um valor do tipo `Object` e, os erros são lançados com uso da função `displayError`. Sempre que há um erro esta função é chamada e é lançada uma excepção de forma a parar a análise semântica sempre que há um erro.

### Dados de contexto utilizados durante a análise semântica

Durante a análise semântica são guardados dados relacionados acerda do contexto, como por exemplo variáveis, dimensões, unidades entre outras. Cada tipo de informação está guardada num `HashMap`.

Criaram-se classes para representar variáveis, dimensões que guardam por exemplo nome, tipo da variável etc...

### Classes importantes

Uma classe importante que utilizamos na análise semântica é a classe `Type` que contém informações sobre tipos, permitindo distinguir uma dimensão de um número real, ou uma lista de inteiros de uma lista de strings por exemplo. A classe contém um atributo `meta` que permite ter recursividade em tipos, deste modo é possível representar listas de listas de inteiros por exemplo. Esta classe contém uma função `matchTypes` que permite comparar tipos bem como uma função `canConvertTo` que indica se é possível transformar um tipo noutro (útil quando se pretende verificar se um tipo pode ser transformado numa `string` (válido em `integer`, `real` , entre outros)).

### Validação de expressões (`expr` na gramática)

Toda a expressão do tipo `expr` retorna um objeto do tipo `Type`, deste modo é possível saber sempre de que tipo é a expressão permitindo assim verificar assignements, comparações etc...

### Unidades e Prefixos

Como toda a unidade (*Unit*) é definida à custa de uma dimensão, sempre que é definida uma *Unit* é adicionado à dimensão o nome da unidade bem como o prefixo (se houver).

Em relação aos prefixos, guardou-se num `HashMap` a lista de prefixos conhecidos e sempre que um `ID` é acedido, este é procurado no `HashMap`.

### Dimensões dependentes

Em relação ás dimensões dependentes de forma a verificar se por exemplo é possível verificar o seguinte código

```
dimension real Length [meter,m];
dimension real Time [second,s];
dimension real Velocity = Length/Time;
dimension real Acceleration = Velocity/Time;

Length l = 1;
Time t = 2;
Acceleration a = (l/t)/t
```

Recorreu-se a um algoritmo bottom up, sempre que é feito um *merge* de duas dimensões (função que dadas duas dimensões retorna uma que tem informação sobre as dimensões base utilizadas) é feita uma pesquisa ás dimensões conhecidas e verifica-se se é possível substituir, no exemplo acima, o analisador semântico ao dar *merge* ao `l/t` iria verificar se existia uma dimensão definida como `Length/Time` e, iria transformar o `l/t` numa dimensão do tipo `Velocity`, de seguida teríamos um `Velocity / Time`, aplicando-se a mesma lógica, é feito o *merge* e faz-se a pesquisa, assume-se então que `(l/t)/t` é do tipo `Acceleration`, o *assignement* ocorre sem erros por serem assim expressões que dão *match*.

A ordem em que é definida a Dimensão mantém-se quando esta é utilizada, deste modo considerou-se que a operação de multiplicação e divisão não são comutativas nesta linguagem ou seja, um `Acceleration * Mass` é considerado diferente de um `Mass * Acceleration`, esta restrição aumenta a legibilidade do código fonte da linguagem.

### Expressões Booleanas (Predicados)

Implementou-se expressões Booleanas no analisador semãntico, que permitem definir relações de **Conjunção** e **Disjunção**. Também foram criados exemplos de teste para testar as mesmas.

## Compilador

Foi usado um visitor que à medida que percorre a árvore gera o código java correspondente com o uso de string templates.

### Classes auxiliares

Foram usadas 3 classes para guardar as dimensões, unidades e variáveis. Estão as 3 interligadas entre si, todas as variáveis tẽm uma unidade, todas as unidades tem uma dimensão e todas as dimensões têm uma unidade por defeito. A obtenção destes valores é feito da seguinte maneira: quando há uma declaração de uma dimensão esta é adicionada a um HashMap, sendo a key o seu nome, e é também criada uma unidade que representa essa dimensão, que da mesma maneira é adicionada a um outro HashMap. No caso da declaração de unidades, há uma adicição ao HashMap correspondente sendo que a unidade tem um campo responsável por guardas a dimensão correspondente. As unidades, se forem ou integers ou reais têm o atributo Unit a null e o atributo Type com o valor correspondente, quando são variáveis associadas a uma unidade o atributo Unit fica com o valor correspondente e o Type fica com o numericType da Dimensão a que esta pertence.
Um exemplo do resultado do uso destas classes: (no fim do exemplo3 estas são as variaveis geradas)

Dimensions

| NAME              | UNIT         | DEPS                           |
| ----------------- | ------------ | ------------------------------ |
| Mass              | gram         | null                           |
| Temperature       | kelvin       | null                           |
| Area              | Area         | [Length, *, Length]            |
| ElectricCurrent   | ampere       | null                           |
| Length            | meter        | null                           |
| Volume            | Volume       | [Length, *, Length, *, Length] |
| Force             | newton       | [Mass, *, Acceleration]        |
| LuminousIntensity | candela      | null                           |
| AmountOfSubstance | mole         | null                           |
| Time              | second       | null                           |
| Acceleration      | Acceleration | [Velocity, /, Time]            |
| Velocity          | Velocity     | [Length, /, Time]              |

UNITS

| NAME         | DIM               | SUFFIX  | MULT   |
| ------------ | ----------------- | ------- | ------ |
| meter        | Length            | m       | 1,0000 |
| gram         | Mass              | g       | 1,0000 |
| mole         | AmountOfSubstance | mol     | 1,0000 |
| candela      | LuminousIntensity | cd      | 1,0000 |
| Velocity     | Velocity          | m/s     | null   |
| ampere       | ElectricCurrent   | A       | 1,0000 |
| second       | Time              | s       | 1,0000 |
| newton       | Force             | N       | null   |
| kelvin       | Temperature       | K       | 1,0000 |
| Area         | Area              | m*m     | null   |
| Volume       | Volume            | m*m*m | null   |
| inch         | Length            | in      | 0,0254 |
| Acceleration | Acceleration      | m/s/s   | null   |

VARIABLES

| NAME | UNIT     | TYPE |
| ---- | -------- | ---- |
| t    | second   | real |
| v    | Velocity | real |
| l    | meter    | real |

### Variáveis auto incrementadas e Variáveis da Gramática

Para a criação de variáveis para efeito de calculos intermédios, foi usada uma função que gera uma nova variável, a par destas variáveis foram adicionadas à gramática as variáveis necessárias. No caso das 'expr' foram adicionadas: eType, varName.

## Contribuições

| Nome                                  | Contribuição                                                                                                                                                                                                                                                                                                     |
| ------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| DAVID JOSÉ ARAÚJO FERREIRA          | *Elaboração completa da gramática no Grammar.g4.<br/>*Colaboração no desenvolvimento do tratamento dos `use`.<br/>*Colaboração no desenvolvimento do GrammarMain.<br/>*Elaboração dos shell scripts finais.                                                                                                       |
| EDUARDO LOPES FERNANDES               | *  Implementação do Compilador                                                                                                                                                                                                                                                                                   |
| GONÇALO LOURENÇO DA SILVA           | * Estudo e implementação da funcionalidade dos `use`, incluindo adaptações necessárias no `compilador` e `analisador semântico`.<br />* Construção do GrammarMain<br />* Pequena Colaboração nos shell scripts finais<br />* Implementação de Expressões Booleanas (predicados) no analisador semântico |
| JOÃO AFONSO PEREIRA FERREIRA         | *  Implementação do Compilador                                                                                                                                                                                                                                                                                   |
| PEDRO DURVAL CABRAL POLÓNIA CRUZEIRO | * Contribuições na implementação da análise semântica e elaboração de exemplos extra.                                                                                                                                                                                                                      |
| TIAGO ANDRÉ LOURENÇO SILVESTRE      | Implementação da análise semântica                                                                                                                                                                                                                                                                             |
