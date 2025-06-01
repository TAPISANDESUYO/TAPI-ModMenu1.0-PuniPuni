//ーーーー>ーー>ー>ーーーーーーーーーーーーーーーーーーーーーー(
//
//
// _______                     _                 
//|__   __|                   (_)                
//   | |      __ _     _ __    _ 
//   | |    / _`  |   | '_ \  | |
//   | |   | (_|  |   |  |_)  | | 
//   |_|    \__,_ \   | .__/  |_|
//                    | |                      
//                    |_|
//
//               ★彡[tapimod]彡★
//         TAPI製ModMenuテンプレ(ぷにぷに)
//         Discord サーバー⬇
//         https://discord.gg/6FQPgbBWGR
//         
//        〘記事〙
//         Modmenu開発に興味ある方いたら一緒に開発しましょう(?)
//         コード的なバグなどありますので自分だけじゃ時間かかるので。
//
//ーーーー>ーー>ー>ーーーーーーーーーーーーーーーーーーーーーー(

#include <list>
#include <vector>
#include <string.h>
#include <pthread.h>
#include <cstring>
#include <jni.h>
#include <unistd.h>
#include <fstream>
#include <iostream>
#include <dlfcn.h> // RTLD_LAZYのため
#include "Includes/Logger.h"
#include "Includes/obfuscate.h"
#include "Includes/Utils.h"
#include "KittyMemory/MemoryPatch.h"
#include "Menu.h"

#define targetLibName OBFUSCATE("libSGF.so")//ここにメインライブラリ名を記入してください(基本的にはlibファイルの中で1番容量が大きいのがメインライブラリですよ！)

#include "Includes/Macros.h"//シンボルを読み込むため

#if defined(__aarch64__)
#include <And64InlineHook/And64InlineHook.hpp>

#else

#include <Substrate/SubstrateHook.h>
#include <Substrate/CydiaSubstrate.h>

#endif

struct My_Patches {
    MemoryPatch//倍速やぷにスローの関数定義
    SpeedReset,
    Speed1,
    Speed2,
    Speed3,
    Speed4,
    Speed5,
    SlowReset,
    Slow1,
    Slow2,
    Slow3,
    Slow4;
} hexPatches;

//------------------------------------
//関数定義
//------------------------------------
bool keiryou = false;//軽量化
bool enemyturn = false;//敵ターン
bool jikkyou = false;//実況
bool ranking = false;//ランキング
bool backButton = false;//戻るボタン

//軽量化
bool (*old_Keiryou)(void *instance);
bool Keiryou(void *instance) {
    if (instance != NULL && keiryou) {
        return false;
    }
    return old_Keiryou(instance);
}

//敵ターン
float (*old_Enemyturn)(void *instance);
float Enemyturn(void *instance) {
    if (instance != NULL && enemyturn) {
        return 0.0;
    }
    return old_Enemyturn(instance);
}

//ランキングボタン
bool (*old_Ranking)(void *instance);
bool Ranking(void *instance) {
    if (instance != NULL && ranking) {
        return true;
    }
    return old_Ranking(instance);
}

//実況
bool (*old_Jikkyou)(void *instance);
bool Jikkyou(void *instance) {
    if (instance != NULL && jikkyou) {
        return true;
    }
    return old_Jikkyou(instance);
}

//戻るボタン
bool (*old_BackButton)(void *instance);
bool BackButton(void *instance) {
    if (instance != NULL && backButton) {
        return true;
    }
    return old_BackButton(instance);
}
//------------------------------------
//シンボル定義
//------------------------------------
void *hack_thread(void *) {
    LOGI(OBFUSCATE("pthread created"));
    do {
        sleep(1);
    } while (!isLibraryLoaded(targetLibName));
    const char *symbol_Scheduler = OBFUSCATE("_ZN3sgf9Scheduler6updateEf");//倍速シンボル
    const char *symbol_Slow = OBFUSCATE("_ZN7b2World4StepEfiii");//遅延シンボル
    void* handle = dlopen(targetLibName, RTLD_NOLOAD);
	
#if defined(__aarch64__) //64bit Mod Menu
    HOOKSYM_LIB("libSGF.so",//軽量化
    "_ZNK3sgf4Mesh9Primitive6renderEv", Keiryou,old_Keiryou);//軽量化
    
    HOOKSYM_LIB("libSGF.so",//敵ターン停止
    "_ZNK21PuzzleTutorialManager15adjustEnemyTimeEf", Enemyturn,old_Enemyturn);//敵ターン停止
    
    HOOKSYM_LIB("libSGF.so",//実況
    "_ZN23LiveMessageButtonWidget13touchPriorityEi", Jikkyou,old_Jikkyou);//実況無効
    
    HOOKSYM_LIB("libSGF.so",//戻るボタン無効
    "_ZN7SO_Back9onTouchedERN3sgf2ui14TouchEventArgsE", BackButton,old_BackButton);//戻るボタン無効
    
    //倍速
    hexPatches.SpeedReset = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Scheduler) + (uintptr_t) 0x210, OBFUSCATE("EBFEFF54"));
    hexPatches.Speed1 = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Scheduler) + (uintptr_t) 0x210, OBFUSCATE("0028281E"));
    hexPatches.Speed2 = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Scheduler) + (uintptr_t) 0x210, OBFUSCATE("0010281E"));
    hexPatches.Speed3 = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Scheduler) + (uintptr_t) 0x210, OBFUSCATE("0090281E"));
    hexPatches.Speed4 = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Scheduler) + (uintptr_t) 0x210, OBFUSCATE("0050291E"));
    hexPatches.Speed5 = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Scheduler) + (uintptr_t) 0x210, OBFUSCATE("00702A1E"));
    
    //ぷにスロー
    hexPatches.SlowReset = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Slow) + (uintptr_t) 0x94, OBFUSCATE("0018281E"));
    hexPatches.Slow1 = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Slow) + (uintptr_t) 0x94, OBFUSCATE("00702A1E"));
    hexPatches.Slow2 = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Slow) + (uintptr_t) 0x94, OBFUSCATE("0050291E"));
    hexPatches.Slow3 = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Slow) + (uintptr_t) 0x94, OBFUSCATE("0010281E"));
    hexPatches.Slow4 = MemoryPatch::createWithHex((uintptr_t)dlsym(handle,symbol_Slow) + (uintptr_t) 0x94 , OBFUSCATE("000C281E"));

	
    #else
    
    LOGI(OBFUSCATE("Done"));
	
#endif

    return NULL;
}

extern "C" {

JNIEXPORT jobjectArray
JNICALL

Java_uk_lgl_modmenu_FloatingModMenuService_getFeatureList(JNIEnv *env, jobject context) {
    jobjectArray ret;                 //Modmenu起動時に出てくるメッセージ
    MakeToast(env, context, OBFUSCATE("サーバーロード中..."), Toast::LENGTH_SHORT);
	
//------------------------------------
//機能の名前とケース
//------------------------------------
    const char *features[] = {
OBFUSCATE("0_Category_メインメニュー"),
    OBFUSCATE("10_SeekBar_倍速_0_5"),
    OBFUSCATE("11_SeekBar_ぷに遅延_0_4"),
    OBFUSCATE("12_Toggle_ワンパン"),
    OBFUSCATE("13_Toggle_無敵"),
    OBFUSCATE("14_Toggle_即勝利"),
    OBFUSCATE("15_Toggle_スコアカンスト"),
    OBFUSCATE("16_Toggle_リザルトスキップ"),
    OBFUSCATE("66_Toggle_軽量化"),
    OBFUSCATE("17_Toggle_敵ターン無効化"),
    OBFUSCATE("21_Toggle_無効化一括"),
    OBFUSCATE("23_Toggle_True_郵便スコアタイベ解放"),
OBFUSCATE("Category_Setting"),
    OBFUSCATE("LogOut_ログアウト"),
    OBFUSCATE("EndApp_アプリ終了"),
    OBFUSCATE("-1_Toggle_機能設定の保存"),
    OBFUSCATE("-3_Toggle_最大サイズに変更"),
    OBFUSCATE("MenuSize_メニューサイズ変更"),
    OBFUSCATE("MenuColor_メニュー色指定"),
    OBFUSCATE("IconSize_アイコンサイズ_0_100"),
    OBFUSCATE("ICONALPHA_アイコンの透明度_0_100"),
OBFUSCATE("Category_SNS LINKs"),
    OBFUSCATE("ButtonLink_Youtube〘TAPICH〙_https://x.gd/m0706"),
    OBFUSCATE("ButtonLink_Twitter〘TAPICH〙_https://x.gd/m0706"),
    OBFUSCATE("ButtonLink_Discord〘Server〙_https://discord.gg/tapimods"),
    OBFUSCATE("SaveAccount_アカウント保存"),
    };
    
    int Total_Feature = (sizeof features / sizeof features[0]);
    ret = (jobjectArray)
            env->NewObjectArray(Total_Feature, env->FindClass(OBFUSCATE("java/lang/String")),
                                env->NewStringUTF(""));
    for (int i = 0; i < Total_Feature; i++)
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(features[i]));
    pthread_t ptid;
    pthread_create(&ptid, NULL, antiLeech, NULL);
    return (ret);
}

JNIEXPORT void JNICALL
Java_uk_lgl_modmenu_Preferences_Changes(JNIEnv *env, jclass clazz, jobject obj,
                                        jint featNum, jstring featName, jint value,
                                        jboolean boolean, jstring str) {
    LOGD(OBFUSCATE("Feature name: %d - %s | Value: = %d | Bool: = %d | Text: = %s"), featNum,
         env->GetStringUTFChars(featName, 0), value,
         boolean, str != NULL ? env->GetStringUTFChars(str, 0) : "");
		 
//------------------------------------
//オフセットなど
//------------------------------------
    switch (featNum) {
case 10://倍速
    switch (value) {
                case 0:
                    hexPatches.SpeedReset.Modify();
                    break;
                case 1:
                    hexPatches.Speed1.Modify();
                    break;
                case 2:
                    hexPatches.Speed2.Modify();
                    break;
                case 3:
                    hexPatches.Speed3.Modify();
                    break;
                case 4:
                    hexPatches.Speed4.Modify();
                    break;
                case 5:
                    hexPatches.Speed5.Modify();
    break;
}
    break;
case 11://ぷに遅延
    switch (value) {
                case 0:
                    hexPatches.SlowReset.Modify();
                    break;
                case 1:
                    hexPatches.Slow1.Modify();
                    break;
                case 2:
                    hexPatches.Slow2.Modify();
                    break;
                case 3:
                    hexPatches.Slow3.Modify();
                    break;
                case 4:
                    hexPatches.Slow4.Modify();
                    break;
}
    break;
case 12: //ワンパン
    PATCH_SYM_OFFSET_SWITCH("_ZN23PuzzleEnemyYoukaiSprite17damageToEnemyDataEi", 0x20 ,"80000054", boolean);
    PATCH_SYM_OFFSET_SWITCH("_ZN11BossDondoro24getBossTargetPosIdRandomEi", 0x130 ,"E003012A", boolean);
    break;
case 13: //無敵
    PATCH_SYM_OFFSET_SWITCH("_ZN11PuzzleScene17calcDamageByEnemyEiRK23PuzzleEnemyYoukaiSprite10DamageType", 0x294,"00 08 21 1E", boolean);
    break;
case 14: //即勝利
    PATCH_SYM_OFFSET_SWITCH("_ZN11PuzzleScene15enemyDeadFinishERKN3sgf2ui9ModelView9EventArgsEb", 0x210 ,"41 01 00 54", boolean);
    break;
case 15: //スコアカンスト
    PATCH_SYM_OFFSET_SWITCH("_ZN11PuzzleScene8addScoreEm", 0x58,"01 C8 14 8B", boolean);
    break;
case 16: //リザルトスキップ
    PATCH_SYM_OFFSET_SWITCH("_ZN11PuzzleScene12onTouchEndedERN3sgf2ui14TouchEventArgsE", 0x204,"1F 04 00 71", boolean);
    break;  
case 17: //敵ターン無効化
    enemyturn = boolean;
    break;
case 18: //敵ターン停止
    PATCH_SYM_OFFSET_SWITCH("_ZN11PuzzleScene11enemiesTurnEf", 0x228, "96 83 08 91", boolean);
    break;
case 21: //無効化一括
    PATCH_SYM_OFFSET_SWITCH("_ZN11StageObject16createFriendIconERN14FlashAnimation10CreateArgsEPK11StageMasterRNSt6__ndk16vectorI11FriendStageNS6_9allocatorIS8_EEEEm", 0x0,"C0 03 5F D6", boolean);
    PATCH_SYM_OFFSET_SWITCH("_ZN8MapScene18openFriendListViewEPK11StageMaster", 0x0,"C0 03 5F D6", boolean);
    PATCH_SYM_OFFSET_SWITCH("_ZN17PowerSealedDialog10initializeEiii", 0x0,"C0 03 5F D6", boolean);
    PATCH_SYM_OFFSET_SWITCH("_ZN10MapProcess20ChkShowNoticeProcess8onAttachEv", 0x34,"29 02 00 35", boolean);
    PATCH_SYM_OFFSET_SWITCH("_ZN10MapProcess28ChkTreasureCollectionProcess8onAttachEv", 0x44,"01 03 00 35", boolean);
    PATCH_SYM_OFFSET_SWITCH("_ZN10MapProcess28ChkLoginStampViewOpenProcess8onAttachEv", 0x30,"48 07 00 35", boolean);
    jikkyou = boolean;//実況無効
    ranking = boolean;//ランキング無効
    break;
case 22: //イベステ解放
    PATCH_SYM_OFFSET_SWITCH("_ZN11EventButton10initializeEPK11EventMasterb", 0xCC, "81 62 80 52", boolean);
    break;
case 23: //郵便スコアタ解放
    PATCH_SYM_OFFSET_SWITCH("_ZN11EventButton10initializeEPK11EventMasterb", 0xCC, "81 62 80 52", boolean);
    break;

    }
}
}

__attribute__((constructor))
void lib_main() {
    pthread_t ptid;
    pthread_create(&ptid, NULL, hack_thread, NULL);
}
