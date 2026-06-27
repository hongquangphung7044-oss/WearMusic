/**
 * MusicFree 插件示例
 * 实际插件可以自定义实现，此处仅作接口演示
 */
const plugin = {
    name: "示例音乐源",
    version: "1.0.0",
    
    /**
     * 搜索歌曲
     * @param {string} keyword 关键词
     * @param {number} page 页码
     * @param {number} limit 每页数量
     * @returns {Object} 搜索结果 {songs: [], playlists: []}
     */
    async search(keyword, page, limit) {
        // 此处在实际插件中调用对应平台的 API
        // 返回格式：
        // {
        //     songs: [
        //         {id: "123", title: "歌曲名", artist: "歌手", ...}
        //     ],
        //     playlists: [...]
        // }
        return {
            songs: [],
            playlists: []
        };
    },

    /**
     * 获取歌曲真实播放地址
     * @param {Object} song 歌曲信息
     * @returns {Object} {url: "播放链接", bitrate: 320}
     */
    async getMediaUrl(song) {
        return {
            url: "",
            bitrate: 320
        };
    },

    /**
     * 获取歌词
     * @param {Object} song 歌曲信息
     * @returns {string} LRC 格式歌词
     */
    async getLyrics(song) {
        return "[00:00.000]暂无歌词";
    }
};
