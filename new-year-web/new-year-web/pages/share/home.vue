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
						<u--text lines="1" color="#FFFFFF" text="摸鱼专家的拜年手势红包">
						</u--text>
					</view>
					<view style="margin-top: 25rpx;">
						<u--text lines="2" color="#FFFFFF"
							text="除夕的爆竹已然绽放，美酒的甘醇散发芬芳，团聚的日子点燃欢笑，幸福和快乐永驻心田。我的祝福早早送上，祝你除夕快乐，合家团圆，猪年吉祥!">
						</u--text>
					</view>
				</view>
			</u-transition>
		</view>
		<u-modal :show="zhijie_lingqushow" confirmText="我也去发一个" @confirm="goHome">
			<view class="slot-content">
				¥ 33.33 领取成功,已存入微信钱包
			</view>
		</u-modal>
		<u-modal :show="zhufuyu_lingqushow" confirmText="确定" @confirm="zhufuyu">
			<view class="slot-content">
				<view>新年祝福语红包需填写祝福语</view>️
				<u--textarea v-model="zhufuyu_value" placeholder="新年祝福语" count maxlength="30"></u--textarea>
			</view>
		</u-modal>
		<u-modal :show="shoushi_lingqushow" confirmText="去拜年" @confirm="goshoushi">
			<view class="slot-content">
				拜年手势红包，需作出拜年动作即可领取
			</view>
		</u-modal>
		<u-notify ref="uNotify" message="Hi uView"></u-notify>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				zhijie_lingqushow: false,
				zhufuyu_lingqushow: false,
				shoushi_lingqushow: false,
				zhufuyu_value: "",
				toast_show: false,
				show: true,
				mode: 'zoom',
				animationData: {},
				timer: null
			}
		},
		onShow() {},
		methods: {
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
					this.shoushi_lingqushow = true
				}, 2000)
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
				this.zhufuyu_lingqushow = false
				this.zhijie_lingqushow = true
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
