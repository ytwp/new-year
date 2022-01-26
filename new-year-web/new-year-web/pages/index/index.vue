<template>
	<view>
		<u-toast ref="uToast"></u-toast>
		<view class="background_image" style="background-color: #fc4437;"></view>
		<image
			style="height: 750rpx;width: 100%; position: absolute; left: 0; bottom: 0; display: block; z-index: -998;"
			src='https://img.yeting.wang/new_year/hhu.png'></image>
		<!-- <image style="height: 100%;width: 100%; position: absolute; left: 0; bottom: 0; display: block; z-index: -997;"
			src='https://img.yeting.wang/new_year/hbjxh.png'></image> -->
		<!-- <image class="background_image"
				src='https://img.yeting.wang/new_year/WechatIMG93215.jpeg?x-oss-process=style/yasuo-30'></image> -->
		<view style="margin-top: 150rpx;"></view>
		<view style="margin: 20rpx;">
			<u--form labelPosition="left" labelWidth="80" :model="from_data">
				<u-form-item label="发送人">
					<u--input v-model="from_data.nickName" border="none" maxlength="20" color="#FFFFFF">
					</u--input>
				</u-form-item>
				<u-form-item label="红包个数">
					<u--input v-model="from_data.num" border="none" type="number" maxlength="3" color="#FFFFFF">
					</u--input>
				</u-form-item>
				<u-form-item label="红包金额">
					<u--input v-model="from_data.amount" border="none" type="digit" maxlength="7" color="#FFFFFF">
					</u--input>
				</u-form-item>
				<u-form-item label="红包类型">
					<u-radio-group v-model="from_data.redPacketType" placement="row">
						<u-radio :customStyle="{marginRight: '16rpx'}" v-for="(item, index) in redPacket_type"
							:key="index" :label="item.name" :name="item.name" labelColor="#FFFFFF">
						</u-radio>
					</u-radio-group>
				</u-form-item>
				<u-form-item label="领取方式">
					<u-radio-group v-model="from_data.receivingMethod" placement="row">
						<u-radio :customStyle="{marginRight: '16rpx'}" v-for="(item, index) in receiving_method"
							:key="index" :label="item.name" :name="item.name" :disabled="item.disabled"
							labelColor="#FFFFFF">
						</u-radio>
					</u-radio-group>
				</u-form-item>
				<u-form-item label="红包祝福">
					<u--input v-model="from_data.redPacketBlessing" border="none" maxlength="30" color="#FFFFFF">
					</u--input>
				</u-form-item>
			</u--form>
			<u-alert title="需支付 3% 手续费 包含(微信支付和运营费用)" type="error" effect="dark" closable></u-alert>
			<u-button type="primary" :loading="pay_loading_show" loadingText="支付中..." text="支付" throttleTime="1000"
				customStyle="margin-top: 10rpx" @click="pay">
			</u-button>
		</view>
		<u-loading-page bg-color='rgba(0, 0, 0, 0.3)' loadingText="支付中..." loadingMode="circle"
			:loading="pay_loading_show"></u-loading-page>
		<u-divider text="红包发送记录" textColor="#FFFFFF" lineColor="#ffffe0"></u-divider>
		<view style="margin-left: 20rpx; margin-right: 20rpx;">
			<scroll-view style="width: 100%;height: 550rpx;" scroll-y="true" @scrolltolower="findRedPacket"
				@refresherrefresh="refreshRedPacket" refresher-enabled="true" :refresher-triggered="list_triggered"
				:refresher-threshold="100" @refresherrestore="onRestore">
				<view v-for="(item, index) in redPacket_list" :key="index"
					style="display: flex; background-color: rgba(231,120,19,0.4);width: 100%;height: 150rpx; margin-top: 10rpx;margin-bottom: 10rpx;padding: 10rpx;">
					<view style="width: 70%;color: #FFFFFF;">
						<view style="display: flex;height: 50rpx;">
							<view style="display: flex; width: 100%;">
								发送人:<text style="padding-left: 8rpx;">{{item.nickName}}</text>
							</view>
						</view>
						<view style="display: flex;height: 50rpx;">
							<view style="display: flex; width: 50%;">
								金额:<text style="padding-left: 8rpx;">{{item.totalFee/100}}</text>
							</view>
							<view style="display: flex;width: 50%;">
								个数:<text style="padding-left: 8rpx;">{{item.num}}</text>
							</view>
						</view>
						<view style="display: flex;height: 50rpx;">
							<view style="display: flex; width: 50%;">
								类型:<text style="padding-left: 8rpx;">{{redPacketTypeM(item.redPacketType)}}</text>
							</view>
							<view style="display: flex;width: 50%;">
								领取:<text style="padding-left: 8rpx;">{{receivingMethodM(item.receivingMethod)}}</text>
							</view>
						</view>
					</view>
					<view style="width: 30%;">
						<text
							style="margin-top: 10rpx; padding-left: 60rpx;color: #FFFFFF;">{{redPacketStatus(item.status)}}</text>
						<button v-show="item.status==10"
							style="margin-top: 27rpx; color: #FFFFFF; background-color: #3c9cff; height: 65rpx; width: 80%; position: relative; border-radius: 35rpx; display: flex;align-items: center;justify-content: center;"
							:data-nickname="from_data.nickName" :data-receivingmethod="item.receivingMethod"
							:data-redpacketid="item.redPacketId" open-type="share">分享</button>
					</view>
				</view>
			</scroll-view>
			<u-gap height="150"></u-gap>
		</view>
		<u-modal :show="login_show" content="获取头像昵称, 用于红包封面展示 ~" closeOnClickOverlay showCancelButton>
			<u-button slot="confirmButton" text="授权" type="primary" @click="getUserInfo"></u-button>
		</u-modal>
		<u-modal title="支付成功" :show="pay_show" :showConfirmButton="false" :closeOnClickOverlay="true"
			@close="pay_show = false">
			<view style="display: flex;margin-top: 20rpx;">
				<u-button slot="confirmButton" text="稍后分享" type="primary" @click="pay_show = false"></u-button>
				<view style=" padding-left: 50rpx; padding-right: 50rpx;"></view>
				<u-button slot="cancelButton" text="分享好友" type="success" @click="pay_show = true" open-type="share">
				</u-button>
			</view>
		</u-modal>
		<u-modal title="支付失败" :show="pay_fail_show" :showConfirmButton="false" :closeOnClickOverlay="true"
			@close="pay_fail_show = false">
			<view style="margin-top: 20rpx; width: 100%;">
				<u--text type="primary" text="请稍后在红包发送记录查看" align="center" size="16"></u--text>
				<view style=" padding-top: 20rpx"></view>
				<u-button slot="confirmButton" text="关闭" type="primary" customStyle="margin-top: 10rpx"
					@click="pay_fail_show = false"></u-button>
			</view>
		</u-modal>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				userInfo: {},
				login_show: false,
				pay_show: false,
				pay_fail_show: false,
				pay_loading_show: false,
				list_triggered: false,
				currentRedPacketId: null,
				currentRedPacket: {},
				redPacket_list: [],
				from_data: {
					nickName: '',
					num: 1,
					amount: 1.88,
					redPacketType: '拼手气红包',
					receivingMethod: '打开即可',
					redPacketBlessing: '祝你新年快乐，合家团圆!'
				},
				receiving_method: [{
						name: '打开即可',
						disabled: false
					},
					{
						name: '写祝福语',
						disabled: false
					},
					{
						name: '手势拜年',
						disabled: true
					}
				],
				redPacket_type: [{
						name: '拼手气红包',
						disabled: false
					},
					{
						name: '普通红包',
						disabled: false
					}
				],
				find_from: {
					page: 1,
					size: 20
				},
				loadStatus: 'loadmore'
			}
		},
		onShareAppMessage(res) {
			console.log(JSON.stringify(res))
			var path = 'pages/share/home'
			if (res.from === 'button') {
				let dataset = res.target.dataset
				if (dataset.nickname == null) {
					console.log(this.currentRedPacket)
					let receivingMethod = ''
					if (this.currentRedPacket.receivingMethod == '打开即可') {
						receivingMethod = '红包打开即可领取'
					} else if (this.currentRedPacket.receivingMethod == '写祝福语') {
						receivingMethod = '祝福语红包'
					} else if (this.currentRedPacket.receivingMethod == '手势拜年') {
						receivingMethod = '手势拜年红包'
					}
					return {
						title: this.currentRedPacket.nickName + " 发来一个 " + receivingMethod,
						path: path + "?userId=" + this.userInfo.userId + "&redPacketId=" + this.currentRedPacketId,
						imageUrl: 'https://img.yeting.wang/new_year/hhu.png'
					}
				} else {
					let receivingMethod = ''
					if (dataset.receivingmethod == 1) {
						receivingMethod = '红包打开即可领取'
					} else if (dataset.receivingmethod == 2) {
						receivingMethod = '祝福语红包'
					} else if (dataset.receivingmethod == 3) {
						receivingMethod = '手势拜年红包'
					}
					return {
						title: dataset.nickname + " 发来一个 " + receivingMethod,
						path: path + "?userId=" + this.userInfo.userId + "&redPacketId=" + dataset.redpacketid,
						imageUrl: 'https://img.yeting.wang/new_year/hhu.png'
					}
				}
			} else {
				return {
					title: '花样拜年红包，快来体验一下',
					path: '/page/index/index',
					imageUrl: 'https://img.yeting.wang/new_year/hhu.png'
				}
			}
		},
		onLoad() {
			this._freshing = false;
			const res = uni.getStorageSync('userInfo');
			if (res) {
				console.log("已登陆：" + res);
				this.userInfo = JSON.parse(res)
				if (this.userInfo.avatarUrl == null) {
					this.login_show = true
				} else {
					this.from_data.nickName = this.userInfo.nickName
				}
				this.findRedPacket()
			} else {
				this.login()
			}
		},
		methods: {
			redPacketStatus(status) {
				if (status == 10) {
					return '已支付'
				} else if (status == 0) {
					return '未支付'
				} else if (status == -10) {
					return '已领完'
				} else if (status == -5) {
					return '已退款'
				}
			},
			redPacketTypeM(redPacketType) {
				if (redPacketType == 1) {
					return '拼手气红包'
				} else if (redPacketType == 2) {
					return '普通红包'
				}
			},
			receivingMethodM(receivingMethod) {
				if (receivingMethod == 1) {
					return '打开即可'
				} else if (receivingMethod == 2) {
					return '写祝福语'
				} else if (receivingMethod == 3) {
					return '手势拜年'
				}
			},
			pay() {
				//校验
				let num = this.from_data.num
				let fee = this.from_data.amount * 100
				let redPacketType = this.from_data.redPacketType
				if (num < 1) {
					uni.showToast({
						title: '最少发1个红包'
					})
					return
				} else if (num > 500) {
					uni.showToast({
						title: '最多发500个红包'
					})
					return
				}
				if (redPacketType == '拼手气红包') {
					if (fee / num < 30) {
						uni.showToast({
							title: '单人最少0.3元'
						})
						return
					} else if (fee / num > 10000) {
						uni.showToast({
							title: '单人最多100元'
						})
						return
					}
				} else if (redPacketType == '普通红包') {
					if (fee < 30) {
						uni.showToast({
							title: '单人最少0.3元'
						})
						return
					} else if (fee > 10000) {
						uni.showToast({
							title: '单人最多100元'
						})
						return
					}
				}

				//支付
				let that = this
				this.pay_loading_show = true
				let from = {
					nickName: this.from_data.nickName,
					num: this.from_data.num,
					amount: this.from_data.amount,
					redPacketBlessing: this.from_data.redPacketBlessing
				}
				if (this.from_data.redPacketType == '拼手气红包') {
					from.redPacketType = 1
				} else if (this.from_data.redPacketType == '普通红包') {
					from.redPacketType = 2
				}
				if (this.from_data.receivingMethod == '打开即可') {
					from.receivingMethod = 1
				} else if (this.from_data.receivingMethod == '写祝福语') {
					from.receivingMethod = 2
				} else if (this.from_data.receivingMethod == '手势拜年') {
					from.receivingMethod = 3
				}
				uni.$u.http.post('/redPacket/send', from).then(res => {
					console.log(JSON.stringify(res))
					that.currentRedPacketId = res.redPacketId
					wx.requestPayment({
						timeStamp: res.timeStamp,
						nonceStr: res.nonceStr,
						package: res.package,
						signType: res.signType,
						paySign: res.paySign,
						success(res) {
							console.log('支付成功')
							that.currentRedPacket = that.from_data
							that.queryPayRes(1)
						},
						fail(res) {
							console.log('支付失败')
							that.pay_loading_show = false
							that.refreshRedPacket()
							uni.showToast({
								title: '支付失败'
							})
						}
					})
				})
			},
			queryPayRes(count) {
				if (count == null) {
					count = 1
				}
				uni.$u.http.post('/redPacket/queryPay', {
					redPacketId: this.currentRedPacketId
				}).then(res => {
					console.log('查询支付结果：' + JSON.stringify(res))
					console.log('查询支付结果：' + res)
					if (res == true) {
						this.pay_show = true
						this.pay_loading_show = false
						this.refreshRedPacket()
					} else {
						if (count < 30) {
							setTimeout(() => {
								this.queryPayRes(++count)
							}, 500)
						} else {
							this.pay_fail_show = true
							this.pay_loading_show = false
							this.refreshRedPacket()
						}
					}
				})
			},
			getUserInfo() {
				console.log("获取用户信息")
				uni.getUserProfile({
					desc: '用于红包展示发送人',
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
							this.from_data.nickName = this.userInfo.nickName
						})
					}
				})
			},
			scrolltolower() {
				this.findRedPacket()
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
							that.findRedPacket()
							if (res.avatarUrl == null) {
								that.login_show = true
							} else {
								that.from_data.nickName = res.nickName
							}
						})
					}
				});
			},
			shareRedPacket() {
				wx.showShareMenu({
					withShareTicket: true,
					menus: ['shareAppMessage', 'shareTimeline']
				})
			},
			findRedPacket() {
				if (this.loadStatus != 'loadmore') {
					return
				}
				uni.$u.http.post('/redPacket/find', this.find_from).then(res => {
					// console.log(JSON.stringify(res))
					let dataLength = res.length
					if (dataLength > 0) {
						for (let i = 0; i < dataLength; i++) {
							this.redPacket_list.push(JSON.parse(JSON.stringify(res[i])))
						}
						this.find_from.page++
						if (dataLength < this.find_from.size) {
							this.loadStatus = 'nomore'
						} else {
							this.loadStatus = 'loadmore'
						}
					} else {
						this.loadStatus = 'nomore'
					}
					this.list_triggered = false
					this._freshing = false
				})
			},
			onRestore() {
				this.list_triggered = 'restore' // 需要重置
				console.log("onRestore");
			},
			refreshRedPacket() {
				if (this._freshing)
					return
				this._freshing = true;
				this.loadStatus = 'loadmore'
				this.find_from.page = 1
				this.redPacket_list = []
				this.findRedPacket()
				this.list_triggered = 'restore'
				this._freshing = false
			}
		}
	}
</script>

<style lang="scss">
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
