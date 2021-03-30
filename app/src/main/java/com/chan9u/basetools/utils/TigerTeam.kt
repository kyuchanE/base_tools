package com.chan9u.basetools.utils

import android.os.Process
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException

/*------------------------------------------------------------------------------
 * DESC    : 디버거 방지
 *------------------------------------------------------------------------------*/

object TigerTeam {
    /**
     * 디버거가 탐자되었는지 확인
     *
     * @return 디비거가 탐지될 경우 true 반환
     */
    fun detect(): Boolean {
        return detectedDebugger() || detectedFrida()
    }

    /**
     * 디비거 탐지
     *
     * @return
     */
    private fun detectedDebugger(): Boolean {
        val pid = Process.myPid() // 현재 실행중인 프로세스의 pid 획득
        val stat = "/proc/$pid/status" // 현재 실행중인 프로세스의 상태를 열람
        val file = File(stat)
        try {
            FileReader(file).use { fr ->
                BufferedReader(fr).use { br ->
                    var line: String?
                    do {
                        line = br.readLine()
                        if (line.notNull) {
                            if (line.indexOf("TracerPid") == 0) { //현재 실행중인 프로세스가 디버거에 의해 제어 되고 있는지 확인. 디버거에 의해 제어되고 있으면 TracerPid가 0이 아닌 디버거 프로세스 pid가 명시되어 있음.
                                val nline = line.trim()
                                val sline = nline.split(":".toRegex()).toTypedArray()
                                if (sline[1].trim().toInt() > 0) { //TracerPid 값이 0이 아니면 (즉 디버거에 의해 제어되고 있으면)
                                    return true // true를 반환, 아니면 false를 반환
                                }
                            }
                        }
                    } while (line != null)
                    return false
                }
            }
        } catch (e: IOException) {
            e.log()
            return false
        }
    }

    /**
     * 프리다 탐지
     *
     * @return
     */
    private fun detectedFrida(): Boolean {
        val pid = Process.myPid() // 현재 실행중인 프로세스의 pid 획득
        val maps = "/proc/$pid/maps" // 현재 실행중인 프로세스의 메모리 맵 열람
        val file = File(maps)
        try {
            FileReader(file).use { fr ->
                BufferedReader(fr).use { br ->
                    var line: String?
                    val sb = StringBuilder()
                    do {
                        line = br.readLine()
                        if (line.notNull) {
                            sb.append(line) //메모리 맵 내용 읽어오기
                        }
                    } while (line != null)
                    val index = sb.indexOf("frida-agent") //현재 실행중인 프로세스 메모리에 프리다 라이브러리가 삽입되어 있는지 확인
                    return index >= 0 // 현재 실행중인 메모리가 프리다에 의해 제어 받고 있으면 true를 반환, 아니면 false를 반환
                }
            }
        } catch (e: IOException) {
            e.log()
            return false
        }
    }
}