package com.chan9u.basetools.utils

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader

/*------------------------------------------------------------------------------
 * DESC    : 루팅여부를 확인
 *------------------------------------------------------------------------------*/

object RootingChecker {
    /**
     * 5가지 루팅 조건을 확인
     *
     * @return
     */
    val isDeviceRooted: Boolean
        get() = checkTestKeys() || checkSuFiles() || checkSuExec() || checkDebugger() || checkFrida()

    /**
     * test-keys가 포함되어 있는지 확인
     *
     * @return
     */
    private fun checkTestKeys(): Boolean {
        val buildTags = android.os.Build.TAGS
        return buildTags.notNull && buildTags.contains("test-keys")
    }

    /**
     * su 파일이 있는지 확인
     *
     * @return
     */
    private fun checkSuFiles(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su", "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su", "/su/bin/su"
        )

        for (path in paths) {
            if (File(path).exists()) return true
        }

        return false
    }

    /**
     * su 실행권한이 있는지 확인
     *
     * @return
     */
    private fun checkSuExec() = try {
        val process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
        BufferedReader(InputStreamReader(process.inputStream)).use { br ->
            val result = br.readLine().notNull
            process.destroy()
            result
        }
    } catch (e : Exception) {
        false
    }


    /**
     * 디비거 탐지
     *
     * @return
     */
    private fun checkDebugger() = try {
        val pid = android.os.Process.myPid()   // 현재 실행중인 프로세스의 pid 획득
        val stat = "/proc/$pid/status"       // 현재 실행중인 프로세스의 상태를 열람
        val file = File(stat)

        FileReader(file).use { fr ->
            BufferedReader(fr).use { br ->
                do { //현재 실행중인 프로세스 상태 읽기
                    val line: String = br.readLine() ?: break

                    if (line.indexOf("TracerPid") == 0) { //현재 실행중인 프로세스가 디버거에 의해 제어 되고 있는지 확인. 디버거에 의해 제어되고 있으면 TracerPid가 0이 아닌 디버거 프로세스 pid가 명시되어 있음.
                        val nline = line.trim()
                        val sline = nline.split(":".toRegex()).toTypedArray()
                        if (Integer.parseInt(sline[1].trim()) > 0) {//TracerPid 값이 0이 아니면 (즉 디버거에 의해 제어되고 있으면)
                            return true    // true를 반환, 아니면 false를 반환
                        }
                    }
                } while (true)
            }
        }

        false
    } catch (e: Exception) {
        false
    }

    /**
     * 프리다 탐지
     *
     * @return
     */
    private fun checkFrida() = try {
        val pid = android.os.Process.myPid()   // 현재 실행중인 프로세스의 pid 획득
        val maps = "/proc/$pid/maps"         // 현재 실행중인 프로세스의 메모리 맵 열람
        val file = File(maps)

        FileReader(file).use { fr ->
            BufferedReader(fr).use { br ->
                val sb = StringBuilder()
                do {
                    val line = br.readLine() ?: break
                    sb.append(line)  // 메모리 맵 내용 읽어오기
                } while (true)

                val index = sb.indexOf("frida-agent") // 현재 실행중인 프로세스 메모리에 프리다 라이브러리가 삽입되어 있는지 확인
                return index >= 0 // 현재 실행중인 메모리가 프리다에 의해 제어 받고 있으면 true를 반환, 아니면 false를 반환
            }
        }
    } catch (e: Exception) {
        false
    }
}