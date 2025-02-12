package com.seonjunkim.radialmenu

class EmojiGraph {
    private var graph = ArrayList<IntArray>()
    private val id2emojiStr = listOf<String>(
        "😠",
        "😧",
        "😰",
        "😲",
        "😁",
        "🥶",
        "😖",
        "😕",
        "🤠",
        "😢",

        "😞",
        "🥸",
        "😵",
        "😓",
        "🤤",
        "🤯",
        "😑",
        "😘",
        "😮‍💨",
        "😶‍🌫",


        "😋",
        "😱",
        "🤮",
        "🤭",
        "🤕",
        "😷",
        "🧐",
        "😮",
        "🤨",
        "🙄",

        "😵‍💫",
        "😤",
        "🤬",
        "😂",
        "🤒",
        "😛",
        "😶",
        "😨",
        "😳",
        "😦",

        "☹️",
        "😬",
        "😃",
        "😄",
        "😅",
        "😀",
        "😆",
        "🥵",
        "🤗",
        "😯",

        "😚",
        "😙",
        "😗",
        "😭",
        "🤥",
        "🤑",
        "🤢",
        "🤓",
        "😐",
        "🥳",

        "😔",
        "😣",
        "🥺",
        "😡",
        "😌",
        "🤣",
        "😥",
        "🤫",
        "😴",
        "😪",

        "🙁",
        "🙂",
        "😇",
        "😍",
        "🥰",
        "😊",
        "😎",
        "🥲",
        "☺️",
        "😏",

        "🤧",
        "😝",
        "🤩",
        "🤔",
        "😫",
        "😒",
        "🙃",
        "😩",
        "😜",
        "😉",

        "🥴",
        "😟",
        "🥱",
        "🤪",
        "🤐"
    )

    constructor(){
        graph.add(intArrayOf(31,32,63))
        graph.add(intArrayOf(9,66,39,91,7))
        graph.add(intArrayOf(21,37))
        graph.add(intArrayOf(15,27,49,38,93))
        graph.add(intArrayOf(43,46,81,51,45))
        graph.add(intArrayOf(56,41,61,10,47))
        graph.add(intArrayOf(56,12,30,84,22))
        graph.add(intArrayOf(9,66,70,40,1))
        graph.add(intArrayOf(23,51,59,64,86))
        graph.add(intArrayOf(53,1,66,62,60))
        graph.add(intArrayOf(56,6,13,18,85))
        graph.add(intArrayOf(29,28,90,67,26))
        graph.add(intArrayOf(56,5,61,6,47))
        graph.add(intArrayOf(18,10,84,61,85))
        graph.add(intArrayOf(4,51,78,50,89))
        graph.add(intArrayOf(15,27,93,3,49))
        graph.add(intArrayOf(58,36,94,28,29))
        graph.add(intArrayOf(4,75,73,74,71))
        graph.add(intArrayOf(56,22,41,10,30))
        graph.add(intArrayOf(16,94,25,28,24))
        graph.add(intArrayOf(4,17,42,23,71))
        graph.add(intArrayOf(2,37))
        graph.add(intArrayOf(18,56,30,10,61))
        graph.add(intArrayOf(4,35,55,88,4))
        graph.add(intArrayOf(29,80,68,90,83))
        graph.add(intArrayOf(16,29,24,19,80))
        graph.add(intArrayOf(16,28,29,94,11))
        graph.add(intArrayOf(15,3,38,49,93))
        graph.add(intArrayOf(16,29,94,26,24))
        graph.add(intArrayOf(16,28,94,26,24))
        graph.add(intArrayOf(56,10,6,61,12))
        graph.add(intArrayOf(63,32,0))
        graph.add(intArrayOf(0,63,31))
        graph.add(intArrayOf(4,65,77,44,14))
        graph.add(intArrayOf(9,66,60,70,7))
        graph.add(intArrayOf(33,14,44,72,71))
        graph.add(intArrayOf(16,58,94,28))
        graph.add(intArrayOf(21,2))
        graph.add(intArrayOf(15,3,49,27,93))
        graph.add(intArrayOf(9,66,34,1,91))
        graph.add(intArrayOf(9,66,70,7,39))
        graph.add(intArrayOf(56,10,61,6,84))
        graph.add(intArrayOf(33,74,48,45,43))
        graph.add(intArrayOf(33,65,59,42,44))
        graph.add(intArrayOf(43,42,45,51,77))
        graph.add(intArrayOf(43,42,44,43,71))
        graph.add(intArrayOf(43,4,81,42,45))
        graph.add(intArrayOf(56,10,6,61,5))
        graph.add(intArrayOf(43,23,51,20,17))
        graph.add(intArrayOf(15,27,3,38,93))
        graph.add(intArrayOf(52,51,78,75,79))
        graph.add(intArrayOf(4,50,75,20,52))
        graph.add(intArrayOf(51,50,71,89,79))
        graph.add(intArrayOf(1,62,91,9,66))
        graph.add(intArrayOf(28,29,24,80,92))
        graph.add(intArrayOf(81,35,88,51,77))
        graph.add(intArrayOf(22,18,30,10,41))
        graph.add(intArrayOf(51,50,76,77,8))
        graph.add(intArrayOf(36,16,54,28,94))
        graph.add(intArrayOf(4,43,46,51,50))
        graph.add(intArrayOf(9,66,7,70,39))
        graph.add(intArrayOf(56,6,10,12,84))
        graph.add(intArrayOf(53,9,66,91,60))
        graph.add(intArrayOf(0,32,31))
        graph.add(intArrayOf(78,50,57,76,8))
        graph.add(intArrayOf(43,46,4,33,77))
        graph.add(intArrayOf(53,9,62,40,39))
        graph.add(intArrayOf(28,29,11,83,90))
        graph.add(intArrayOf(28,92,90,69,24))
        graph.add(intArrayOf(94,68,90,68,67))
        graph.add(intArrayOf(9,66,40,7,39))
        graph.add(intArrayOf(75,51,52,89,79))
        graph.add(intArrayOf(43,51,74,17,71))
        graph.add(intArrayOf(82,43,46,73,17))
        graph.add(intArrayOf(4,75,51,20,71))
        graph.add(intArrayOf(4,51,50,78,71))
        graph.add(intArrayOf(75,51,57,89,46))
        graph.add(intArrayOf(65,75,77,52,89))
        graph.add(intArrayOf(43,50,75,51,89))
        graph.add(intArrayOf(50,78,64,89,51))
        graph.add(intArrayOf(24,94,90,25,69))
        graph.add(intArrayOf(46,43,4,35,88))
        graph.add(intArrayOf(43,73,46,45,42))
        graph.add(intArrayOf(28,29,67,90,54))
        graph.add(intArrayOf(18,10,61,12,87))
        graph.add(intArrayOf(10,61,87,18,41))
        graph.add(intArrayOf(51,75,52,71,79))
        graph.add(intArrayOf(10,84,85,6,18))
        graph.add(intArrayOf(81,35,55,51,71))
        graph.add(intArrayOf(78,51,71,52,79))
        graph.add(intArrayOf(28,24,80,68,69))
        graph.add(intArrayOf(9,66,39,1,7))
        graph.add(intArrayOf(28,29,83,67,90))
        graph.add(intArrayOf(38,27,3,15,49))
        graph.add(intArrayOf(36,28,29,25,90))
    }

    fun getAdj(id: Int): IntArray{
        return graph[id]
    }

    fun emojiIdtoString(id: Int): String{
        return id2emojiStr[id]
    }
}