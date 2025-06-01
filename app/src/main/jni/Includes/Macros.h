// thanks to shmoo and joeyjurjens for the usefull stuff under this comment.
#ifndef ANDROID_MOD_MENU_MACROS_H
#define ANDROID_MOD_MENU_MACROS_H

#if defined(__aarch64__) //Compile for arm64 lib only
#include <And64InlineHook/And64InlineHook.hpp>

#else //Compile for armv7 lib only. Do not worry about greyed out highlighting code, it still works
#include <Substrate/SubstrateHook.h>
#include <Substrate/CydiaSubstrate.h>

#endif

void hook(void *offset, void* ptr, void **orig)
{
#if defined(__aarch64__)
    A64HookFunction(offset, ptr, orig);
#else
    MSHookFunction(offset, ptr, orig);
#endif
}

void *getAddressFromSymbol(const char *libName, const char *symbolName) {
    void *handle = dlopen(libName, RTLD_LAZY);
    if (!handle) {
    
        return nullptr;
    }

    void *address = dlsym(handle, symbolName);
    if (!address) {
        
        dlclose(handle);
        return nullptr;
    }

    dlclose(handle);
    return address;
}

#define HOOK(offset, ptr, orig) hook((void *)getAbsoluteAddress(targetLibName, string2Offset(OBFUSCATE(offset))), (void *)ptr, (void **)&orig)
#define HOOK_LIB(lib, offset, ptr, orig) hook((void *)getAbsoluteAddress(OBFUSCATE(lib), string2Offset(OBFUSCATE(offset))), (void *)ptr, (void **)&orig)


#define HOOK_NO_ORIG(offset, ptr) hook((void *)getAbsoluteAddress(targetLibName, string2Offset(OBFUSCATE(offset))), (void *)ptr, NULL)
#define HOOK_LIB_NO_ORIG(lib, offset, ptr) hook((void *)getAbsoluteAddress(OBFUSCATE(lib), string2Offset(OBFUSCATE(offset))), (void *)ptr, NULL)

#define HOOKSYM(sym, ptr, org) hook(dlsym(dlopen(targetLibName, 4), OBFUSCATE(sym)), (void *)ptr, (void **)&org)
#define HOOKSYM_LIB(lib, sym, ptr, org) hook(dlsym(dlopen(OBFUSCATE(lib),4), OBFUSCATE(sym)), (void *)ptr, (void **)&org)

#define HOOKSYM_NO_ORIG(sym, ptr)  hook(dlsym(dlopen(targetLibName, 4), OBFUSCATE(sym)), (void *)ptr, NULL)
#define HOOKSYM_LIB_NO_ORIG(lib, sym, ptr) hook(dlsym(dlopen(OBFUSCATE(lib), 4), OBFUSCATE(sym)), (void *)ptr, NULL)

std::vector<MemoryPatch> memoryPatches;
std::vector<uint64_t> offsetVector;
// Patching a offset without switch.
void patchOffset(const char *fileName, uint64_t offset, std::string hexBytes, bool isOn) {

    MemoryPatch patch = MemoryPatch::createWithHex(fileName, offset, hexBytes);

    //Check if offset exists in the offsetVector
    if (std::find(offsetVector.begin(), offsetVector.end(), offset) != offsetVector.end()) {
        //LOGE(OBFUSCATE("Already exists"));
        std::vector<uint64_t>::iterator itr = std::find(offsetVector.begin(), offsetVector.end(), offset);
        patch = memoryPatches[std::distance(offsetVector.begin(), itr)]; //Get index of memoryPatches vector
    } else {
        memoryPatches.push_back(patch);
        offsetVector.push_back(offset);
        //LOGI(OBFUSCATE("Added"));
    }

    if (!patch.isValid()) {
        LOGE(OBFUSCATE("Failing offset: 0x%llu, please re-check the hex"), offset);
        return;
    }
    if (isOn) {
        if (!patch.Modify()) {
            LOGE(OBFUSCATE("Something went wrong while patching this offset: 0x%llu"), offset);
        }
    } else {
        if (!patch.Restore()) {
            LOGE(OBFUSCATE("Something went wrong while restoring this offset: 0x%llu"), offset);
        }
    }
}

void patchOffsetSym(uintptr_t absolute_address, std::string hexBytes, bool isOn) {

    MemoryPatch patch = MemoryPatch::createWithHex(absolute_address, hexBytes);

    //Check if offset exists in the offsetVector
    if (std::find(offsetVector.begin(), offsetVector.end(), absolute_address) != offsetVector.end()) {
        //LOGE(OBFUSCATE("Already exists"));
        std::vector<uint64_t>::iterator itr = std::find(offsetVector.begin(), offsetVector.end(), absolute_address);
        patch = memoryPatches[std::distance(offsetVector.begin(), itr)]; //Get index of memoryPatches vector
    } else {
        memoryPatches.push_back(patch);
        offsetVector.push_back(absolute_address);
        //LOGI(OBFUSCATE("Added"));
    }

    if (!patch.isValid()) {
        LOGE(OBFUSCATE("Failing offset: 0x%llu, please re-check the hex"), absolute_address);
        return;
    }
    if (isOn) {
        if (!patch.Modify()) {
            LOGE(OBFUSCATE("Something went wrong while patching this offset: 0x%llu"), absolute_address);
        }
    } else {
        if (!patch.Restore()) {
            LOGE(OBFUSCATE("Something went wrong while restoring this offset: 0x%llu"), absolute_address);
        }
    }
}
void patchSymOffsetSwitch(const char *sym, int offset, const char* hexBytes, bool boolean) {
    void *sym_address = dlsym(dlopen(targetLibName, 4),sym);


    if (sym_address != NULL) {
        void *patch_address = reinterpret_cast<char *>(sym_address) + offset;
        patchOffsetSym(reinterpret_cast<uintptr_t>(patch_address), hexBytes, boolean);
    }
}
void patchSymAute(const char *sym, int offset, const char* hexBytes, bool isOn) {
    void *sym_auteaddress = dlsym(dlopen(targetLibName, 4),sym);


    if (sym_auteaddress != NULL) {
        void *patch_address = reinterpret_cast<char *>(sym_auteaddress) + offset;
        patchOffsetSym(reinterpret_cast<uintptr_t>(patch_address), hexBytes, isOn);
    }
}
#define PATCH_SYM_OFFSET_SWITCH(sym, offset, hex, boolean) patchSymOffsetSwitch(OBFUSCATE(sym),offset,OBFUSCATE(hex),boolean)
#define PATCH_SYM_AUTE(sym, offset, hex) patchSymAute(OBFUSCATE(sym),offset,OBFUSCATE(hex),true)

#define PATCH_SYM_SWITCH(sym, hex, boolean) patchOffsetSym((uintptr_t)dlsym(dlopen(targetLibName, 4), OBFUSCATE(sym)), OBFUSCATE(hex), boolean)
#define PATCH_LIB_SYM_SWITCH(lib, sym, hex, boolean) patchOffsetSym((uintptr_t)dlsym(dlopen(lib, 4), OBFUSCATE(sym)), OBFUSCATE(hex), boolean)

#define HEX_PATCH_SYM(Symbol, Offset, Hex, PatchObject) \
    PatchObject = MemoryPatch::createWithHex((uintptr_t)dlsym(handle, Symbol) + (uintptr_t) Offset, OBFUSCATE(Hex));


#define PATCH(offset, hex) patchOffset(targetLibName, string2Offset(OBFUSCATE(offset)), OBFUSCATE(hex), true)
#define PATCH_LIB(lib, offset, hex) patchOffset(OBFUSCATE(lib), string2Offset(OBFUSCATE(offset)), OBFUSCATE(hex), true)

#define PATCH_SYM(sym, hex) patchOffset(dlsym(dlopen(targetLibName, 4), OBFUSCATE(sym)), OBFUSCATE(hex), true)
#define PATCH_LIB_SYM(lib, sym, hex) patchOffset(dlsym(dlopen(lib, 4), OBFUSCATE(sym)), OBFUSCATE(hex), true)

#define PATCH_SWITCH(offset, hex, boolean) patchOffset(targetLibName, string2Offset(OBFUSCATE(offset)), OBFUSCATE(hex), boolean)
#define PATCH_LIB_SWITCH(lib, offset, hex, boolean) patchOffset(OBFUSCATE(lib), string2Offset(OBFUSCATE(offset)), OBFUSCATE(hex), boolean)
/*#define PATCH_LIB_SYM_OFFSET_SWITCH(lib, sym, offset, hex, boolean) \
    do { \
        void sym_address = dlsym(dlopen(lib, RTLD_LAZY), OBFUSCATE(sym)); \
        if (sym_address != NULL) { \
            voidpatch_address = reinterpret_cast<char >(sym_address) + offset; \
            patchOffsetSym(reinterpret_cast<uintptr_t>(patch_address), OBFUSCATE(hex), boolean); \
        } \
    } while (0)
#define PATCH_SYM_OFFSET_SWITCH(sym, offset, hex, boolean) \
    do { \
        void *sym_address = dlsym(dlopen(targetLibName, RTLD_LAZY), OBFUSCATE(sym)); \
        if (sym_address != NULL) { \
            void *patch_address = reinterpret_cast<char *>(sym_address) + offset; \
            patchOffsetSym(reinterpret_cast<uintptr_t>(patch_address), OBFUSCATE(hex), boolean); \
        } \
    } while (0)
#define PATCH_SYM_OFFSET(sym, offset, hex, true) \
    do { \
        void *sym_address = dlsym(dlopen(targetLibName, RTLD_LAZY), OBFUSCATE(sym)); \
        if (sym_address != NULL) { \
            void *patch_address = reinterpret_cast<char *>(sym_address) + offset; \
            patchOffsetSym(reinterpret_cast<uintptr_t>(patch_address), OBFUSCATE(hex), true); \
        } \
    } while (0)*/
#define PATCH_SYM_SWITCH(sym, hex, boolean) patchOffsetSym((uintptr_t)dlsym(dlopen(targetLibName, 4), OBFUSCATE(sym)), OBFUSCATE(hex), boolean)
#define PATCH_LIB_SYM_SWITCH(lib, sym, hex, boolean) patchOffsetSym((uintptr_t)dlsym(dlopen(lib, 4), OBFUSCATE(sym)), OBFUSCATE(hex), boolean)
#define RESTORE(offset) patchOffset(targetLibName, string2Offset(OBFUSCATE(offset)), "", false)
#define RESTORE_LIB(lib, offset) patchOffset(OBFUSCATE(lib), string2Offset(OBFUSCATE(offset)), "", false)

#define RESTORE_SYM(sym) patchOffsetSym((uintptr_t)dlsym(dlopen(targetLibName, 4), OBFUSCATE(sym)), "", false)
#define RESTORE_LIB_SYM(lib, sym) patchOffsetSym((uintptr_t)dlsym(dlopen(lib, 4), OBFUSCATE(sym)), "", false)

#endif //ANDROID_MOD_MENU_MACROS_H

