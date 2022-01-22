<template>
	<view>
		<view class="background_image" style="background-color: #fc4437;"></view>
		<image
			style="height: 750rpx;width: 100%; position: absolute; left: 0; bottom: 0; display: block; z-index: -998;"
			src='https://img.yeting.wang/new_year/hhu.png'></image>
		<image style="height: 100%;width: 100%; position: absolute; left: 0; bottom: 0; display: block; z-index: -997;"
			src='https://img.yeting.wang/new_year/hbjxh.png'></image>
		<!-- <image class="background_image"
				src='https://img.yeting.wang/new_year/WechatIMG93215.jpeg?x-oss-process=style/yasuo-30'></image> -->
		<view style="margin-top: 150rpx;"></view>
		<view style="margin: 20rpx;">
			<u--form labelPosition="left" labelWidth="80" :model="from_data">
				<u-form-item label="红包个数">
					<u--input v-model="from_data.num" border="none"></u--input>
				</u-form-item>
				<u-form-item label="红包金额">
					<u--input v-model="from_data.amount" border="none"></u--input>
				</u-form-item>
				<u-form-item label="红包类型">
					<u-radio-group v-model="from_data.redpacketType" placement="row">
						<u-radio :customStyle="{marginRight: '16rpx'}" v-for="(item, index) in redpacket_type"
							:key="index" :label="item.name" :name="item.name">
						</u-radio>
					</u-radio-group>
				</u-form-item>
				<u-form-item label="领取方式">
					<u-radio-group v-model="from_data.receivingMethod" placement="row">
						<u-radio :customStyle="{marginRight: '16rpx'}" v-for="(item, index) in receiving_method"
							:key="index" :label="item.name" :name="item.name">
						</u-radio>
					</u-radio-group>
				</u-form-item>
			</u--form>
			<u-button type="primary" text="提交" customStyle="margin-top: 10rpx" @click="goshare11"></u-button>
		</view>
		<u-divider text="红包发送记录" textColor="#FFFFFF" lineColor="#ffffe0"></u-divider>
		<u-list @scrolltolower="scrolltolower">
			<u-list-item v-for="(item, index) in indexList" :key="index">
				<button @click="goshare11">{{item}}</button>
			</u-list-item>
		</u-list>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				indexList: [1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5],
				from_data: {
					redpacketType: '拼手气红包',
					receivingMethod: '打开即可'
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
						disabled: false
					}
				],
				redpacket_type: [{
						name: '拼手气红包',
						disabled: false
					},
					{
						name: '普通红包',
						disabled: false
					}
				]
			}
		},
		methods: {
			goshare11() {
				uni.navigateTo({
					url: '/pages/share/share?id=1&name=uniapp'
				})
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
