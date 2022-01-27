<template>
	<view>
		<view style="">
			<camera device-position="front" flash="off" frame-size="small" @error="error" @initdone="xiangji"
				style="width: 100%; height: 100vh;"></camera>
		</view>
		<view v-show="gesture_error" class="background_image"
			style="z-index: 999;margin-top: 170rpx;width: 530rpx;margin-left: 110rpx;">
			<u-alert type="warning" :show-icon="true" :title="gesture_title" :center="true"></u-alert>
		</view>
		<image class="background_image" src="https://img.yeting.wang/new_year/7.png"></image>
		<u-modal :show="res_lingqushow" :confirmText="redPacketReceive.buttonContext"
			@confirm="resBut(redPacketReceive.buttonMethod)">
			<view class="slot-content">
				{{redPacketReceive.message}}
			</view>
		</u-modal>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				ctx: null,
				img: null,
				flag: false,
				res_lingqushow: false,
				redPacketUserId: null,
				redPacketId: null,
				gesture_error: false,
				gesture_title: '',
				listener: null,
				redPacketReceive: {}
			}
		},
		onLoad(res) {
			this.redPacketUserId = res.userId
			console.log("发红包userId：" + this.redPacketUserId)
			this.redPacketId = res.redPacketId
			console.log("红包Id：" + this.redPacketId)
			const userInfo = uni.getStorageSync('userInfo')
			this.userInfo = JSON.parse(userInfo)
		},
		onReady() {
			this.ctx = uni.createCameraContext()
			this.xiangji()
			this.flag = true
		},
		methods: {
			xiangji() {
				this.listener = this.ctx.onCameraFrame((frame) => {
					if (this.flag == true) {
						this.flag = false
						console.log(frame.data instanceof ArrayBuffer, frame.width, frame.height)
						let data = new Uint8ClampedArray(frame.data);
						let list = new Array
						for (var i = 0; i < data.length; i += 4) {
							list.push([data[i], data[i + 1], data[i + 2], data[i + 3]])
						}
						uni.$u.http.post('/redPacket/infer', {
							imgArray: list,
							width: frame.width,
							height: frame.height,
							redPacketUserId: this.redPacketUserId,
							redPacketId: this.redPacketId,
						}).then(data => {
							console.log(data)
							if (data.gestureStatus) {
								this.listener.stop()
								this.gesture_error = false
								this.redPacketReceive = data
								this.res_lingqushow = true
							} else {
								this.flag = true
								this.gesture_error = true
								this.gesture_title = data.message
								console.log("未识别到拜年姿势");
							}
						}).catch(err => {
							console.log("err:" + err)
							setTimeout(() => {
								this.flag = true
							}, 3000);
						})
					}
				})
				this.listener.start({
					success: function(res) {
						console.log("开始监听");
					}
				});
			},
			resBut(buttonMethod) {
				if (buttonMethod == -10) {
					uni.navigateBack({
						delta: 1
					})
				} else {
					this.goHome()
				}
			},
			goHome() {
				uni.redirectTo({
					url: '/pages/index/index'
				});
			},
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
