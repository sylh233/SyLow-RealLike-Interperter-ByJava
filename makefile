run: build
	java -cp out cbyj.cbyj repl
# -cp :classpath; package cbyj: out/cbyj/cbyj.class
# java用"."作为路径

SRC_DIR ?= . lexer parser func
SRC ?= $(addprefix src/cbyj/,$(addsuffix /*.java,$(SRC_DIR)))

build: $(SRC)
	javac -d out $(SRC)
# -d: class放到的地方，会按package作为文件夹，放到out/里面
# javac则是用"/"
# javac只识别包的名称
# 而jdtls按项目文件夹（例如带.git的）下的src的子目录为package名
# 例如src/cbyj为package cbyj;

FILE ?= try.syrl
FILE_PATH ?= $(addprefix scripts/,$(FILE))
runfile: $(FILE_PATH) build
	java -cp out cbyj.cbyj file $(FILE_PATH)

runload: $(FILE_PATH) build
	java -cp out cbyj.cbyj load $(FILE_PATH)
