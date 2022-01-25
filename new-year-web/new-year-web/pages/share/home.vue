<template>
	<view>
		<image class="background_image"
			src='https://img.yeting.wang/new_year/WechatIMG93215.jpeg?x-oss-process=style/yasuo-30'></image>
		<view :animation="animationData" style="padding-left: 100rpx;padding-right: 100rpx;margin-top: 500rpx;"
			@tap="$u.throttle(rotateAndScale, 2000)">
			<u-transition :show="show" :mode="mode" duration="1000">
				<image
					style="width: 550rpx;height: 815rpx;display: block;position: absolute; z-index: 100; border-radius: 13px; box-shadow: 0 15px 30px rgba(0,0,0,0.8); "
					src="https://img.yeting.wang/new_year/hongbao.png"></image>
				<view style="margin-top: 510rpx;z-index: 200;padding-left:60rpx ;padding-right: 80rpx;">
					<view style="display:flex;">
						<u-avatar size="30" shape="circle"
							src="https://img.yeting.wang/new_year/hhu.png?x-oss-process=style/yasuo-30"></u-avatar>
						<view style="margin-left: 15rpx;"></view>
						<view style="width: 360rpx; margin-top: 7rpx;">
							<u--text lines="1" color="#FFFFFF"
								:text="redPacket.nickName+receivingMethodM(redPacket.receivingMethod)">
							</u--text>
						</view>
					</view>
					<view style="margin-top: 25rpx;">
						<u--text lines="2" align="center" color="#FFFFFF" :text="redPacket.redPacketBlessing">
						</u--text>
					</view>
				</view>
			</u-transition>
		</view>
		<u-modal :show="res_lingqushow" :confirmText="redPacketReceive.buttonContext" @confirm="resBut(redPacketReceive.buttonMethod)">
			<view class="slot-content">
				{{redPacketReceive.message}}
			</view>
		</u-modal>
		<u-modal :show="zhufuyu_lingqushow" confirmText="确定" @confirm="zhufuyu" closeOnClickOverlay
			@close="zhufuyu_lingqushow = false">
			<view class="slot-content">
				<view>新年祝福语红包需填写祝福语</view>️
				<u--textarea v-model="zhufuyu_value" placeholder="新年祝福语" count maxlength="30"></u--textarea>
			</view>
		</u-modal>
		<u-modal :show="shoushi_lingqushow" confirmText="去拜年" @confirm="goshoushi" closeOnClickOverlay
			@close="shoushi_lingqushow = false">
			<view class="slot-content">
				手势拜年红包，需做出拜年动作即可领取
			</view>
		</u-modal>
		<u-modal :show="login_show" content="获取头像昵称, 用于红包展示领取人 ~" closeOnClickOverlay showCancelButton>
			<u-button slot="confirmButton" text="授权" type="primary" @click="getUserInfo"></u-button>
		</u-modal>
		<u-loading-page bg-color='rgba(0, 0, 0, 0.2)' loadingText="领取中..." loadingMode="circle"
			:loading="receive_loading_show"></u-loading-page>
		<u-notify ref="uNotify" message="Hi uView"></u-notify>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				userInfo: {},
				login_show: false,
				receive_loading_show: false,
				res_lingqushow: false,
				zhufuyu_lingqushow: false,
				shoushi_lingqushow: false,
				zhufuyu_value: "",
				toast_show: false,
				show: true,
				mode: 'zoom',
				animationData: {},
				timer: null,
				redPacket: {},
				redPacketUserId: null,
				redPacketId: null,
				redPacketReceive: {},
			}
		},
		onLoad(res) {
			this.redPacketUserId = res.userId
			console.log("发红包userId：" + this.redPacketUserId)
			this.redPacketId = res.redPacketId
			console.log("红包Id：" + this.redPacketId)
			this.getRedPacket()
			const userInfo = uni.getStorageSync('userInfo')
			if (userInfo) {
				console.log("已登陆：" + userInfo);
				this.userInfo = JSON.parse(userInfo)
				if (this.userInfo.avatarUrl == null) {
					this.login_show = true
				}
			} else {
				this.login()
			}
		},
		onShow() {},
		methods: {
			receivingMethodM(receivingMethod) {
				if (receivingMethod == 1) {
					return '的新年红包'
				} else if (receivingMethod == 2) {
					return '的祝福语红包'
				} else if (receivingMethod == 3) {
					return '的手势拜年红包'
				}
			},
			login() {
				let that = this
				uni.login({
					provider: 'weixin',
					success: function(res) {
						console.log(res);
						var code = res.code
						uni.$u.http.post('/login/login', {
							"code": code,
							"userInfo": {}
						}).then(res => {
							console.log(JSON.stringify(res))
							that.userInfo = res
							uni.setStorageSync('userInfo', JSON.stringify(res));
							if (res.avatarUrl == null) {
								that.login_show = true
							}
						})
					}
				});
			},
			getUserInfo() {
				console.log("获取用户信息")
				uni.getUserProfile({
					desc: '用于红包展示领取人',
					success: (res) => {
						console.log(res.userInfo)
						uni.$u.http.post('/login/update', {
							"code": null,
							"userInfo": res.userInfo
						}).then(res => {
							let userInfo = res
							this.userInfo = userInfo
							console.log(JSON.stringify(userInfo))
							uni.setStorageSync('userInfo', JSON.stringify(userInfo));
							this.login_show = false
						})
					}
				})
			},
			rotateAndScale() {
				var animation = uni.createAnimation({
					duration: 500,
					timingFunction: 'ease',
				})
				this.animation = animation
				var next = true;
				// 无限循环动画
				this.timer = setInterval(function() {
					if (next) {
						// 你要执行动画链(详见文档)
						this.animation.scale(0.95).step()
						next = !next;
					} else {
						// 你要执行动画链(详见文档)
						this.animation.scale(1).step()
						next = !next;
					}
					// 导出动画
					this.animationData = this.animation.export()
				}.bind(this), 500)
				setTimeout(() => {
					clearInterval(this.timer)
					this.receiveRedPacket()
				}, 2000)
			},
			receiveRedPacket() {
				if (this.redPacket.receivingMethod == 1) {
					this.receive_loading_show = true
					//ui 已经节流，所以这里不需要节流
					this.receive()
				} else if (this.redPacket.receivingMethod == 2) {
					this.zhufuyu_lingqushow = true
				} else if (this.redPacket.receivingMethod == 3) {
					this.shoushi_lingqushow = true
				}
			},
			receive() {
				console.log("领取红包")
				uni.$u.http.post('/redPacket/receive', {
					redPacketId: this.redPacketId,
					userId: this.redPacketUserId
				}).then(res => {
					console.log(JSON.stringify(res))
					this.redPacketReceive = res
					this.receive_loading_show = false
					this.res_lingqushow = true
					// if (res.status) {
					// 	this.receive_loading_show = false
					// 	this.res_lingqushow = true
					// } else {
					// 	this.receive_loading_show = false
					// 	console.log("领取失败")
					// }
				})
			},
			getRedPacket() {
				uni.$u.http.post('/redPacket/share/get', {
					redPacketId: this.redPacketId,
					userId: this.redPacketUserId
				}).then(res => {
					console.log(JSON.stringify(res))
					this.redPacket = res
				})
			},
			resBut(buttonMethod) {
				if (buttonMethod == -10) {
					this.res_lingqushow = false
				} else {
					this.goHome()
				}
			},
			goHome() {
				uni.redirectTo({
					url: '/pages/index/index'
				});
			},
			goshoushi() {
				uni.navigateTo({
					url: '/pages/share/shoushi'
				})
			},
			zhufuyu() {
				this.receive_loading_show = true
				this.zhufuyu_lingqushow = false
				uni.$u.throttle(this.receive, 1500)
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
		z-index: -999;
	}
</style>
