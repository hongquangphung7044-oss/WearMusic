/**
 * WearMusic 示例插件
 * 演示 MusicFree 风格插件的基本结构
 * 
 * 插件必须导出一个包含以下方法的对象：
 * - search(keyword, page, limit)
 * - getMediaUrl(song) 
 * - getLyrics(song)
 * - getPlaylists() (可选)
 * 
 * 这个示例插件返回硬编码数据，实际插件应调用对应的音乐 API
 */

const demoPlugin = {
    name: "示例音乐源",
    version: "1.0.0",
    author: "WearMusic",
    
    /**
     * 搜索歌曲
     * @param {string} keyword 搜索关键词
     * @param {number} page 页码，从 1 开始
     * @param {number} limit 每页数量
     * @returns {Promise<{songs: Array, playlists: Array}>}
     */
    async search(keyword, page = 1, limit = 20) {
        // 实际插件这里应该调用对应平台的搜索 API
        // 例如：网易云、QQ音乐、酷狗等
        console.log(`[Plugin] Searching: ${keyword}, page: ${page}`);
        
        // 返回示例数据
        return {
            songs: [
                {
                    id: "demo_song_1",
                    title: "示例歌曲 - " + keyword,
                    artist: "示例歌手",
                    album: "示例专辑",
                    duration: 180000,  // 毫秒
                    coverUrl: "https://example.com/cover.jpg",
                    mediaUrl: ""
                }
            ],
            playlists: [
                {
                    id: "demo_playlist_1",
                    name: keyword + "相关歌单",
                    description: "这是一个示例歌单",
                    coverUrl: ""
                }
            ]
        };
    },

    /**
     * 获取歌曲真实播放地址
     * @param {Object} song 歌曲信息（由 search 返回）
     * @returns {Promise<string>} 实际可播放的音频 URL
     */
    async getMediaUrl(song) {
        console.log(`[Plugin] Getting media URL for: ${song.title}`);
        
        // 实际插件应调用对应接口获取真实播放地址
        // 例如网易云音乐的 /song/url 接口
        return "https://example.com/demo.mp3";
    },

    /**
     * 获取歌词
     * @param {Object} song 歌曲信息
     * @returns {Promise<string>} LRC 格式歌词
     */
    async getLyrics(song) {
        console.log(`[Plugin] Getting lyrics for: ${song.title}`);
        
        // 返回示例 LRC 歌词
        return `[00:00.000] 示例歌词 - ${song.title}
[00:03.000] 这是第一行示例歌词
[00:06.000] 这是第二行示例歌词
[00:09.000] 这里是歌曲的高潮部分
[00:12.000] 歌词可以包含时间和文本`;
    },

    /**
     * 获取推荐歌单（可选）
     * @returns {Promise<Array>}
     */
    async getPlaylists() {
        return [
            {
                id: "recommend_1",
                name: "每日推荐",
                description: "为你精选",
                coverUrl: ""
            }
        ];
    }
};

// 导出插件（通用兼容写法）
if (typeof module !== 'undefined' && module.exports) {
    module.exports = demoPlugin;
} else if (typeof window !== 'undefined') {
    window.plugin = demoPlugin;
}
