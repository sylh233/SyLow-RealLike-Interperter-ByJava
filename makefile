run: build
	java -cp out cbyj.cbyj
# -cp :classpath; package cbyj: out/cbyj/cbyj.class
# java用"."作为路径
build: src/cbyj/*.java src/cbyj/lexer/*.java src/cbyj/parser/*.java
	javac -d out src/cbyj/*.java src/cbyj/lexer/*.java src/cbyj/parser/*.java
# -d: class放到的地方，会按package作为文件夹，放到out/里面
# javac则是用"/"
# javac只识别包的名称
# 而jdtls按项目文件夹（例如带.git的）下的src的子目录为package名
# 例如src/cbyj为package cbyj;

FILE ?= try.syrl
runfile: build scripts/$(FILE)
	java -cp out cbyj.cbyj scripts/$(FILE)
