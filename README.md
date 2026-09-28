# 使用方法（Usage）

## REPL模式（REPL Mode）
在makefile所在目录内执行
```shell
make
```

## FILE模式（FILE Mode）
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

## 表达式组与返回语句
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
$ var [# length] [= value] // 声明语句，#表示要声明一个数组，=后面是初始化的表达式；
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
```
作为函数
>>> $ String = "Write :line \"Hello World\""
Start:
Value: $ String = Write :line "Hello World"
>>> `String
Start:
Hello World
Value: Hello World
```

字符串语句在执行时有自己的作用域：
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

实现闭包：
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


## 表达式和运算符
表达式分为：
```
一元运算(! 逻辑否,- 负号)、

二元运算(+-*/ 四则运算,> < >= <= 比较,== != 判等)、

表达式组([...])、

字面量(123 'a'(97) 整数,1.5 浮点数,"Hello" "Write \"Hello\" " 字符串)、

变量(SyRL是动态类型的)、

赋值(a = b)、

逻辑(and or)、

数组( (1,"Hello",Write 233) )、

选择(? index (1,2,3) 或 # index (1,2,3) )、

和语句（语句表达式）
```
下面简单说一下字符串中的转义字符和选择表达式
### 转义
目前只支持4种转义
```
\n 换行 
\t 制表符
\\ 反斜线
\" 双引号
```
### 选择表达式与数组
选择表达式的操作对象是数组，变量和字符串；

字面数组本身可以被执行或作为右值，例如：
```
>>> (Write 1,Write 2) // 依次执行，返回值为一个等长数组，每项为原项的返回值，无局部作用域（谨慎在此声明变量）

>>> $ array = (1,2,3)
```

可以直接声明一个空数组：
```
仅声明
$ array # 10 // array为长度为10，每项为NULL的空数组
赋值 单值
$ array2 # 10 = "Hello" // array2的第一项为"Hello"，其余为NULL
赋值 部分数组
$ array3 # 10 = ("Hello","World") // array3的前几项为后面的数组的内容，其余为NULL
```

选择表达式具有分支语句和数组取项两种功能：
```
作为一个"历史"遗留问题，?和#都可以作为选择表达式的开头

分支语句
? 1 (Write 0,Write 1) // 输出1（同时返回1，因为Write语句返回后面表达式的值）
# 0 (Write 0,Write 1) // 输出0

取值和赋值
a = ? 1 (1,2,3) // a = 2
$ array = (100,20,3)
b = ? 1 array // b = 20
? 0 array = 6 // array = (6,20,3)，作为左值的选择表达式后面必须是一个变量数组，而不是字面量或字符串
c = ? 0 "Hello" // c = "H" 是一个字符串而不是一个字符，SyRL的字符等同于一个整数（ASCII）

关键字true，false
? true (0,1) // 0，true在选择表达式的index一项中等同于0（而不是1），这是为了使它更接近通常使用的if语句
? false (0,1) // 1
```
