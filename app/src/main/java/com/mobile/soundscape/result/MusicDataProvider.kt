package com.mobile.soundscape.result

object MusicDataProvider {

    fun createDummyData(): List<MusicModel> {
        val list = ArrayList<MusicModel>()

        // 1. Blue Valentine
        list.add(MusicModel(
            title = "Blue Valentine",
            artist = "NMIXX",
            albumCover = "https://i.namu.wiki/i/bH8QAtP6ft_iRUrN-BtoeFn3luR8WEu8KDeR6sdbc-onsH8h6QoUQkAUTr--INMIILYTMrMEiDVr45rN9ojBVA.webp"
        ))

        // 2. Good Goodbye
        list.add(MusicModel(
            title = "Good Goodbye",
            artist = "화사",
            albumCover = "https://image.bugsm.co.kr/album/images/500/41305/4130508.jpg"
        ))

        // 3. ONE MORE TIME
        list.add(MusicModel(
            title = "ONE MORE TIME",
            artist = "ALLDAY PROJECT",
            albumCover = "https://image.bugsm.co.kr/album/images/200/41335/4133502.jpg?version=20251206014315"
        ))

        // 4. SPAGHETTI
        list.add(MusicModel(
            title = "SPAGHETTI (feat. j-hope of BTS)",
            artist = "LE SSERAFIM",
            albumCover = "https://image.bugsm.co.kr/album/images/200/41314/4131460.jpg?version=20251028011200"
        ))

        // 5. 멸종위기사랑
        list.add(MusicModel(
            title = "멸종위기사랑",
            artist = "이찬혁",
            albumCover = "https://image.bugsm.co.kr/album/images/200/41229/4122969.jpg?version=20250729020842"
        ))

        // 6. Drowning
        list.add(MusicModel(
            title = "Drowning",
            artist = "WOODZ",
            albumCover = "https://image.bugsm.co.kr/album/images/200/40839/4083984.jpg?version=20250315015832"
        ))

        // 7. FOCUS
        list.add(MusicModel(
            title = "FOCUS",
            artist = "Hearts2Hearts",
            albumCover = "https://image.bugsm.co.kr/album/images/200/41289/4128980.jpg?version=20251022002830"
        ))

        // 8. 뛰어(JUMP)
        list.add(MusicModel(
            title = "뛰어(JUMP)",
            artist = "BLACKPINK",
            albumCover = "https://image.bugsm.co.kr/album/images/200/41229/4122947.jpg?version=20250712005750"
        ))

        // 9. NOT CUTE ANYMORE
        list.add(MusicModel(
            title = "NOT CUTE ANYMORE",
            artist = "아일릿",
            albumCover = "https://image.bugsm.co.kr/album/images/200/41342/4134249.jpg?version=20251129010313"
        ))

        // 10. XOXZ
        list.add(MusicModel(
            title = "XOXZ",
            artist = "IVE",
            albumCover = "https://image.bugsm.co.kr/album/images/200/41260/4126044.jpg?version=20250828003754"
        ))


        return list
    }
}