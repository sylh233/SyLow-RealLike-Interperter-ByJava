# 使用方法（Usage）

以下指令皆在makefile所在目录下执行

## REPL模式（REPL Mode）

### 使用make
在makefile所在目录内执行
```shell
make
```
或
```
make run
```

### 直接执行
可以先用make进行编译
```
make build
```
或是直接用javac自行编译，源码全部在src/目录下（详情可见makefile）

然后用java执行
```
java -cp out cbyj.cbyj
```

## FILE模式（FILE Mode）
### 使用make
```shell
make runfile FILE="[filename]"
```
filename的父目录是scripts文件夹

如需更改启动文件的位置请自行更改makefile的"scripts/"这里
```
runfile: build scripts/$(FILE)
	java -cp out cbyj.cbyj scripts/$(FILE)
	#					   ^^^^^^^^ Here
```

### 直接执行
```
java -cp out cbyj.cbyj scripts/[filename]
```

# 基础语法简介（Basic Syntax Brief）
## 入口（Entry）
```syrl
=>"Expression"
```
FILE模式只执行紧跟在"=>"后的一个表达式（Expression）；

在SyRL语法中，语句（Statement）属于表达式的子类，所以也可以跟一个语句

## REPL模式
```
>>> "Expression","Expression",...,"Expression"
```
或
```
>>> "Expression";"Expression";...;"Expression"
```
REPL模式没有入口，一次执行由','/';'分隔的一串表达式；

每次执行的作用域都是全局作用域，且不会清除

## 结果
```
Start:
	......
Value:......
```
文件执行后的结果有Start和Value两部分；

Start的内容是控制台输出（stdout），即使用Write语句；

Value是表达式的值

## 主程序结构：表达式组与返回语句
```
["Expression","Expression",...,"Expression"]
```
表达式组是用','分隔的一串表达式，其值为最后一个表达式的值；

若表达式是一个语句，则执行该语句，并也求得一个返回值；

使用'<= "Expression"'可以中断表达式组，并使得其值为'<='后的表达式的值，类似与"return"；

一般的主程序以'=>'加'[]'的组合组成，例如：
```syrl
=>[
	"Expression",
	...,
	"Expression",
	0
]
```
末尾的0就类似"return 0;"，令整个程序返回一个0作为返回码；

## 语句
在此，语句是表达式的子类型，可以产生副作用并返回一个值;

这是一个REPL模式下的stdout语句：
```
>>> Write "Hello World"
```

在FILE模式下可以写作：
```syrl
=>[
	Write "Hello World",
	0
]
```

Write语句的结构为：
```
Write [:mode]* "Expression"
```
[]内的内容表示可选项，*表示其可以重复；
Write的mode有noun（默认），line和file（未实现），
分别对应print，println和文件写入；
例如：
```
Write "hello" // hello
Write :noun "hello" // 同上
Write :line "hello" // 后面有换行 = "hello\n"
Write :line :noun "hello" // :line被:noun覆盖掉了，还是hello
```

目前支持的语句包括：
```
Write [:mode]* "Expression" // stdout 打印到控制台
Read // stdin 返回一个控制台的输入，类型为字符串
$ var [? length] [= value] // 声明语句，?(0.2.0及以前是#)表示要声明一个数组，=后面是初始化的表达式；
							// 返回字符串"$ var = value"，
							// 但value是初始化表达式一个求值而非表达式本身，
							// 借助这个特性可以实现闭包
`String // 将String作为代码并执行它，代码的结构要求和REPL模式相同；
		// 元编程特性
{Statement;Statement;...;Statement;} // 语句块，只能包含语句而非表达式
	// （其实语句有一种子类型为纯表达式语句，因此表达式也可以是语句的子类型，可以看出SyRL的语句和表达式其实是很模糊的概念），
	// 每个语句后都必须有';'；语句块返回值NULL
@{...} // 循环语句，@后面跟一个语句块
< // break语句，用于退出循环
<= // return语句，用于提前返回表达式组
"Expression" // 纯表达式；在语句块这种只可容纳语句的结构中，表达式被解析为纯表达式语句（其实没什么区别）
```
赋值和选择等属于表达式而非语句，故不再此列

### 字符串语句
作为SyRL的一个基本上"唯一"有特色的特性，字符串语句可以实现类似于函数或者闭包的效果；

在SyRL正式实现函数和闭包的官方特性前，可以使用其作为替代:

- 作为函数：
```
>>> $ String = "Write :line \"Hello World\""
Start:
Value: $ String = Write :line "Hello World"
>>> `String
Start:
Hello World
Value: Hello World
```

- 字符串语句在执行时有自己的作用域：
```
>>> $ fn = "$ a = 1"
Start:
Value: "$ fn = $ a = 1"
>>> `fn
Start:
Value: "$ a = 1"
>>> a
Start:
Undefined Variable a! // 这里的a是在字符串中声明的，在字符串语句执行完后就被释放了
[Line 1]
Value: null
```

- 实现闭包：
```
=>[
	$ a = 10,
	Write :line ["a = " + a],
	$ fn = "{\n\t",
	$ b = 6,
	fn = fn + [$ b = a], // 利用声明语句返回值的等号右边是计算后的结果的特性
	// 而且在此处将$ b = a放入一个具有局部作用域的表达式组，在声明执行完并退出后表达式组后，局部的b变量会被释放，不会污染全局作用域
	Write :line "b = " + b,
	fn = fn + ";\n\tWrite :line [\"b = \" + b];\n}",
	Write :line "Enclosure is \n" + fn,
	a = 20,
	Write :line ["now a = " + a],
	`fn,
	"Finish!"
]

Start:
a = 10
b = 6 // 可以看到确实没有改变外部b的值
Enclosure is 
{
	$ b = 10; // 这里的b不再是a，而是a的值10
	Write :line ["b = " + b];
}
now a = 20 // 哪怕修改了a的值，b的值也不变
b = 10
Value: "Finish!"
```

## 基础值类型
- 整数
```
233 // 32位
```
- 字符
```
'a' // 16位
```
- 浮点数
```
233.333, 1.0 // 64位
```
以上三种类型为数字类型，在运算中按照"字符 < 整数 < 浮点数"的顺序进行类型提升

"字符"参与数学运算则提升为"整数"；
若算式中没有"浮点数"则结果为整数，否则为"浮点数"
```
'a' // 'a'
'a' + 'a' // 194
'a' + 1 // 98
1 + 1 // 2
1 + 1.0 // 2.0
1.0 + 1.0 // 2.0
'a' * 2 // 194
```

- 字符串
```
"Hello", "a"
```
- 布尔值
```
true, True
false, False
```
- NULL
```
null, NULL
```
任何简单类型都可以进行加法运算，若含有非数字类型，则结果为字符串
```
'a' + "b" // "ab"
1 + "a" // "1a"
true + "1" // "true1"
false + '2' // "false2"
null + '3' // "NULL3"
```

另外还有两种特殊类型——数组和函数，将在后面提到

## 表达式和运算符
表达式分为：
- 一元运算(! 逻辑否,- 负号)、
- 二元运算(+-*/ 四则运算,> < >= <= 比较,== != 判等)、
- 表达式组([...])、
- 字面量(123 'a'(97) 整数,1.5 浮点数,"Hello" "Write \"Hello\" " 字符串)、
- 变量(SyRL是动态类型的)、
- 赋值(a = b)、
- 逻辑(and or)、
- 数组( (1,"Hello",Write 233) )、
- 选择(? index (1,2,3))、
- 语句（语句表达式）
- 函数调用(*添加于0.3.0* # function arguments)

下面简单说一下字符串中的转义字符和选择表达式
### 转义
目前只支持4种转义
```
\n // 换行 
\t // 制表符
\\ // 反斜线
\" // 双引号
```
### 选择表达式与数组
选择表达式的操作对象是数组，变量和字符串；

字面数组本身可以被执行或作为右值，例如：
```
>>> (Write 1,Write 2) // 依次执行，返回值为一个等长数组，每项为原项的返回值，无局部作用域（谨慎在此声明变量）

>>> $ array = (1,2,3)
```

- 可以直接声明一个空数组：
```
$ array ? 10 // array为长度为10，每项为NULL的空数组
```

- 赋值:单值
```
$ array2 ? 10 = "Hello" // array2的第一项为"Hello"，其余为NULL
```

- 赋值:部分数组
```
$ array3 ? 10 = ("Hello","World") // array3的前几项为后面的数组的内容，其余为NULL
```

选择表达式具有分支语句和数组取项两种功能：

~~作为一个"历史"遗留问题，?和#都可以作为选择表达式的开头~~(此问题在0.3.0已解决，决定将以前的'#'都改为'?'。最初选择'#'作为数组取项和数组声明的符号是因为它有"No."的含义，而分支语句则是用'?'。但是这两种方法在实现过程中被统一了，由于难以取舍，所以就让这两种记号就都可以使用。但是0.3.0版本我们带来了函数，函数调用的"call"跟'#'(电话上有'#'，打电话是"call")有点相适合，本来也想过用在邮箱中常用的'@'表示函数调用，但是'@'本身就有点像一个循环，所以保留了他作为loop的符号。)

- 分支语句
```
? 1 (Write 0,Write 1) // 输出1（同时返回1，因为Write语句返回后面表达式的值）
```

- 取值和赋值
```
a = ? 1 (1,2,3) // a = 2
$ array = (100,20,3)
b = ? 1 array // b = 20
? 0 array = 6 // array = (6,20,3)，作为左值的选择表达式后面必须是一个变量数组，而不是字面量或字符串
c = ? 0 "Hello" // c = "H" 是一个字符串而不是一个字符，SyRL的字符等同于一个整数（ASCII）
```

- 布尔值(true，false)
```
? true (0,1) // 0，true在选择表达式的index一项中等同于0（而不是1），这是为了使它更接近通常使用的if语句
? false (0,1) // 1
```
布尔值作为条件项根据后面值类型的情况有所不同。

后面为字面数组时，不会报越界，静默返回NULL，这是为了符合常用的if-else语句的习惯
```
? true () // 会返回NULL，不算越界
? true (1) // 1
? false (1) // 同第一种
```
类似于C语言的：
```example.c
if (flase) {...}; // 不会执行后面的语句块
```

后面为字符串时，直接报越界
```
? true "abc" // 非法，直接算越界
```

后面为数组变量时，把布尔值当索引（后面提到）
```
$ a = (flase: 2, true: 1),
? true a // 1
```

- 索引
索引使得数组有类似字典的特性

索引可以是任何类型，且优先级高于数字序号
```
$ f = # () [0],
$ a = (1: 1, true: 2, "abc": 3, f: 4),
? f a // 4
? 1 a // 1 而不是 2，因为1先作为索引而不是序号

$ b = (1: 1, true: 2, "abc": 3, # () [0]: 4),
? # () [0] b // NULL，这里的索引和条件"相等"而不"相同"，因为他们在用于实现的语言java里是不同的对象实例，而上面用变量的情况则可以正常匹配

? "a" ("b":1) // NULL
? 1 (1:"a") // "a"
? 1 (0) // 越界。因为1不是任何一个项的索引，所以按序号处理，会发生越界
```
使用索引在不匹配任何情况时之后返回NULL，而不会报错

为了得到索引，会对其求值，所以如果索引是语句表达式则会被运行，可能产生副作用
```
>>> ? 0 (Write 0:0,Write 1:1)

Start:
01
Value: 0
```

使用索引可以实现类似于C的结构体、Python的字典、Java的Map或Js的Object的简单效果，也可以用来模拟switch case语句（它其实就是为了实现switch case的功能而生的）

## 语法糖
目前的语法糖仅有用于循环的几种

### 循环
'@'循环本身是纯粹的循环，只能手动用'<'退出或用'<='退出到更外侧的表达式组;

为了使得循环写起来更简单，因此对于循环加了几个语法糖：
- for循环：
```
for($ i = 0;i < 10;i = i + 1){
	Write :line "hello";
}
```

- while循环：
```
while([i < 10]){
	Write :line "hello";
	i = i + 1;
}
```

- break语句:
```
break // <
```

- return语句:
```
return "Expression" // <= "Expression"
```

## 注释
采用模仿C与C++风格的注释
```
//	单行注释
/*
	多行注释
*/
```

## 函数
### 函数调用
由调用符号'#'后接函数名，再后接一个实参数组组成
(其实大部分语言中的函数调用本不必使用什么特殊符号，如这里的'#'，他们只需要识别一个标识符后面的括号，就知道这是个函数。但是我们支持一种以数组型变量为实参的调用方法，只识别括号就显得不太够用了。为了实现简单，所以采取了特殊符号来调用函数的方式。)
```
# function (argument1,argument2,...,argumentN) // 字面数组调用

$ arguments = (argument1,argument2,...,argumentN),
# function arguments // 数组变量调用
```

### 原生函数(Native Function)

参考*Crafting Interpreters by Robert Nystrom*，实现了原生函数"clock"，用于查看当前时间戳
```
// 两种调用方法
# clock () // 返回字符串 "HH:mm:ss yyyy/MM/dd"

$ a = (),
# clock a
```

### 定义函数
SyRL的函数都是匿名函数，因此定义函数即声明一个值为匿名函数的变量：
```
$ function = # (parameter1,...,parameterN) Expression(Body)
```
匿名函数的语法为：
```
# (parameter1,...,parameterN) Expression(Body)
```
和函数调用的arguments不同的是，parameters必须是一个项为只能标识符的"数组"(只是看起来像字面的数组，但其实只是用'('和')'包裹，用','分隔的一串标识符（Identifier），并不是真正意义上的数组。另外如果这里的形参不是标识符，则会报一个解析期错误而不是运行时错误。这也能说明他跟通常的数组在解析上有本质的不同。)，而不能是变量

匿名函数也可以直接调用，但是显得有点奇怪：
```
# # () Expression () // 其实就等于 Expression
# [# () Expression] () // 结构更清晰的写法
```

### 函数的作用域
函数有自己的作用域。

即使函数体为一个没有作用域的表达式，而不是表达式组和语句块这种自带作用域的表达式，但它还是在函数的作用域里。

函数的作用域情况如下：
```
{ 函数被调用的作用域 E
	{ 函数自己的作用域（形参被声明的作用域） P
		{ （如果有）作为函数体的表达式组或语句块的作用域 B
		
		}
	}
}
```


