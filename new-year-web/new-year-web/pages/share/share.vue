<template>
	<view>
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
				left_ear: {
					x: 342.8333435058594,
					y: 0,
				}
			}
		},
		onLoad() {
			this.ctx = uni.createCameraContext()
			const FMS = uni.getFileSystemManager()
			setInterval(() => {
				this.ctx.takePhoto({
					quality: 'high',
					success: (res) => {
						// console.log(res.tempImagePath)
						FMS.readFile({
							filePath: res.tempImagePath,
							encoding: 'base64',
							success(response) {
								const picNameR = response.data
								// console.log(picNameR)
								uni.$u.http.post('/predict/infer', {
									img: picNameR
								}).then(data => {
									console.log(data)
									let p = data[0]
									this.left_ear = p['left_ear']
									console.log(this.left_ear)
								}).catch(err => {
									console.log("err:" + err)
								})
							}
						})
					}
				})
				// console.log(11)
				// // console.log(this.img)
				// uni.canvasPutImageData({
				// 	canvasId: 'myCanvas',
				// 	x: 0,
				// 	y: 0,
				// 	width: this.img.width,
				// 	height: this.img.height,
				// 	data: this.img.data,
				// 	success(res) {
				// 		uni.canvasToTempFilePath({
				// 			x: 0,
				// 			y: 0,
				// 			width: this.img.width,
				// 			height: this.img.height,
				// 			canvasId: 'myCanvas',
				// 			success(res) {
				// 				console.log(res.tempFilePath)
				// 			}
				// 		})

				// 	}
				// })
			}, 5000);
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
			xiangji(e) {
				// console.log("11"+e)
				this.ctx = uni.createCameraContext()
				const listener = this.ctx.onCameraFrame((frame) => {
					this.img = frame
					console.log(frame.data instanceof ArrayBuffer, frame.width, frame.height)
				})
				listener.start({
					success: function(res) {
						// console.log("开始监听");
						// setInner = setInterval(function() {
						// 	console.log("开始监听111111");
						// }, 500);
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
