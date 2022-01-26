<template>
	<view>
		<canvas id="firstCanvas"></canvas>
		<camera device-position="front" flash="off" frame-size="small" @error="error" @initdone="xiangji"
			style="width: 100%; height: 100vh;"></camera>
		<image class="background_image" src="https://img.yeting.wang/new_year/7.png"></image>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				ctx: null,
				img: null,
				flag: false
			}
		},
		onLoad() {
			this.ctx = uni.createCameraContext()
			// const FMS = uni.getFileSystemManager()
			setInterval(() => {
				// this.ctx.takePhoto({
				// 	quality: 'high',
				// 	success: (res) => {
				// 		console.log(res.tempImagePath)
				// 		FMS.readFile({
				// 			filePath: res.tempImagePath,
				// 			encoding: 'base64',
				// 			success(response) {
				// 				const picNameR = response.data
				// 				// console.log(picNameR)
				// 				uni.$u.http.post('/predict/infer', {
				// 					img: picNameR
				// 				}).then(data => {
				// 					console.log(data)
				// 				}).catch(err => {
				// 					console.log("err:" + err)
				// 				})
				// 			}
				// 		})
				// 	}
				// })
				console.log(11)
				this.flag = true
			}, 3000);
		},
		onReady() {
			this.xiangji()
		},
		methods: {
			takePhoto(ctx) {
				ctx.takePhoto({
					quality: 'high',
					success: (res) => {
						console.log("ljjjjj:" + res.tempImagePath)
					}
				})
			},
			xiangji() {
				const listener = this.ctx.onCameraFrame((frame) => {
					if (this.flag == true) {
						this.flag = false
						console.log(frame.data instanceof ArrayBuffer, frame.width, frame.height)
						let data = new Uint8ClampedArray(frame.data);
						let list = new Array
						for (var i = 0; i < data.length; i += 4) {
							list.push([data[i], data[i + 1], data[i + 2], data[i + 3]])
						}
						uni.$u.http.post('/test', {
							imgArray: list,
							width: frame.width,
							height: frame.height
						}).then(data => {
							console.log(data)
						}).catch(err => {
							console.log("err:" + err)
						})
					}
				})
				listener.start({
					success: function(res) {
						console.log("开始监听");
					}
				});

			}
		}
	}
</script>

<style>
	.background_image {
		/* filter: blur(1px); */
		position: absolute;
		left: 0;
		top: 0;
		display: block;
		width: 100%;
		height: 100%;
		z-index: 899;
	}
</style>
