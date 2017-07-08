CFLAGS = -I../../include -I../../remoteApi -I. -I/mnt/c/Developments/AndroidSDK/ndk-bundle/platforms/android-14/arch-arm/usr/include -Wall -D_Included_extApiJava=1 -DNON_MATLAB_PARSING -DMAX_EXT_API_CONNECTIONS=255 -fPIC
CC=/usr/bin/arm-linux-gnueabi-g++
OS = $(shell uname -s)
ifeq ($(OS), Linux)
	CFLAGS += -D__linux -shared
	EXT = so
else
	CFLAGS += -D__APPLE__ -dynamiclib -current_version 1.0
	EXT = dylib
endif

all: 
	@rm -f lib/*.$(EXT)
	@rm -f *.o 
	$(CC) $(CFLAGS) -c ../../remoteApi/extApi.c -o extApi.o
	$(CC) $(CFLAGS) -c ../../remoteApi/extApiPlatform.c -o extApiPlatform.o
	@mkdir -p lib
	$(CC) extApi.o extApiPlatform.o -o lib/libremoteApiJava.$(EXT) -lpthread -ldl $(CFLAGS)
