# 圈子扩展协议


## 目录

+ [摘要] 
+ [需求]
+ [术语]
+ [角色和权限]
+ [用户用例]
	- [查询圈子]
	- [查询圈子详情]
	- [查询圈子成员列表]
	- [查询已加入的圈子列表]
	- [申请加入圈子]
+ [圈子成员用例]
	- [发送圈子消息]
	- [修改圈子名片]
	- [邀请用户]
	- [退出圈子]
+ [圈子所有者用例]
	- [创建圈子]
	- [修改圈子信息]
	- [踢人]
	- [销毁圈子]

## 摘要 
本文定义了一个XMPP扩展协议用于实现持久化的多用户社交圈子功能，即多个XMPP用户可以在同一个圈子中互相交流信息，类似`QQ`的聊天群，用户可以获得自己加入的圈子，创建属于自己的圈子也可以邀请其他用户加入圈子等。

本协议定义了一个简单的圈子控制模型，他保护基本的圈子创建、查询、申请、邀请、踢人等等。

**作者：张智**


## 需求 
本文描述了XMPP圈子的最小功能集：

1. 	每个圈子被标示为<groupid@service> (例如：<123@group.jabber.org>)，这里的	`groupid`是圈子的唯一标示而`service`是圈子服务运行的主机名。
2. 	一个用户登录后服务端自动发送出席信息给该用户所加入的每一个圈子。
3. 	一个用户登出后服务端自动发送类型为`unavailable`的出席信息给用户加入的所有圈	子。
4. 	用户可以创建属于自己的圈子，并成为该圈子的所有者。
5. 	允许圈子所有者邀请用户加入自己所有的圈子。
6. 	允许用户申请加入某圈子。
7. 	允许圈子成员退出加入的圈子。
8. 	允许圈子成员发送圈子消息。
9. 	允许圈子成员修改自己的圈子昵称。
10. 允许用户查询圈子服务器中的圈子列表。
11. 允许用户获取自己曾经加入过的圈子列表。
12. 允许圈子所有者修改圈子信息。
13. 允许圈子所有者将圈子用户踢出圈子。
14. 允许圈子所有者解散圈子。


## 术语  




## 角色和权限 

#### 以下是已定义的角色：

名称          		| 说明
-------------------	|------------------------------------
用户	(User)			| 普通的XMPP用户
圈子成员(Member)		| 已经加入圈子的XMPP用户
圈子所有者(Owner)		| 创建圈子的XMPP用户，同时也是圈子成员


#### 以下是角色相关的权限列表：

权限					| 圈子成员	| 圈子所有者
-------------------	|----------	|-------------------------
发送圈子消息			| 是			| 是
接收圈子消息			| 是			| 是
邀请用户加入圈子		| 否			| 是
处理用户的加入申请		| 否			| 是
修改自己的圈子昵称		| 是			| 是
修改圈子资料			| 否			| 是
踢人					| 否			| 是
解散圈子				| 否			| 是


## 用户用例

### 查询圈子

#### 例子1.用户提交查询表单

```
<iq from='user@skysea.com' to='group.skysea.com' id='v1' type='set'>
	<query xmlns='jabber:iq:search'>
		<x xmlns='jabber:x:data' type='submit'>
		    <field var='name'  type='text-single'>
		      <value>圈子名称</value>
		    </field>
		    <field var='category' type='list-single'>
		      <value>圈子分类</value>
		    </field>
	  	</x>
	  	<set xmlns='http://jabber.org/protocol/rsm'>
	      <max>10</max>
	      <index>0</index>
	    </set>	        		
  	</query>
</iq>
```
#### 例子2.服务返回查询出的圈子列表

```
<iq from='group.skysea.com' to='user@skysea.com' id='v1' type='result'>
	<query xmlns='jabber:iq:search'>
		<x xmlns='jabber:x:data' type='result'>
			<reported>
				<field var='jid'/>
				<field var='openness'/>
				<field var='owner'/>
		        <field var='name'/>
		        <field var='logo'/>
		        <field var='memberCount' />
		        <field var='subject'/>
	      	</reported>
			<item>
				<field var='jid'> <value>1@group.skysea.com</value> </field>
				<field var='openness'> <value>PUBLIC</value> </field>
				<field var='owner'> <value>admin</value> </field>
		        <field var='name'> <value>一起狂欢</value> </field>
		        <field var='logo'> <value>test.jpg</value> </field>
		        <field var='memberCount'> <value>100</value> </field>
		        <field var='subject'> <value>开心不开心的请跟我来！</value> </field>
		    </item>
		    .
		    [8 more items]
		    .
		    <item>
				<field var='jid'> <value>10@group.skysea.com</value> </field>
		    	<field var='openness'> <value>AFFIRM_REQUIRED</value> </field>
				<field var='owner'> <value>admin</value> </field>
		        <field var='name'> <value>80后交友</value> </field>
		        <field var='logo'> <value>test2.jpg</value> </field>
		        <field var='memberCount'> <value>70</value> </field>
		        <field var='subject'> <value>80后的伙伴们，一起hi吧！</value> </field>
		    </item>
		</x>
		<set xmlns='http://jabber.org/protocol/rsm'>
	      <first index='0'>1</first>
	      <last>10</last>
	      <count>800</count>
	    </set>
	</query>
</iq>
```

服务端根据查询条件返回圈子列表，但返回列表中不包括 `PRIVATE` 类型的圈子。


### 查询圈子详情

#### 列子1.用户查询圈子详细信息

```
<iq from='user@skysea.com' to='100@group.skysea.com' id='v2' type='get'>
  <query xmlns='http://skysea.com/protocol/group' node='info'/>
</iq>
```

#### 列子2.服务返回圈子详细信息

```
<iq from='100@group.skysea.com' to='user@skysea.com' id='v2' type='result'>
  <query xmlns='http://skysea.com/protocol/group' node='info'>
  	<x xmlns='jabber:x:data' type='result'>
		<field var='jid'> <value>100@group.skysea.com</value> </field>
		<field var='owner'> <value>admin</value> </field>
		<field var='logo'> <value>logo.jpg</value> </field>
		<field var='name'> <value>一起狂欢</value> </field>
		<field var='memberCount'> <value>100</value> </field>
		<field var='subject'> <value>今晚一醉方休！</value> </field>
		<field var='description'> <value>欢迎80，90，00后的少年们的加入！</value> </field>
		<field var='openness'> <value>PUBLIC</value> </field>
		<field var='createTime'> <value>2001-07-04T12:08:56Z</value> </field>
	</x>
  </query>
</iq>
```
创建时间：`createTime`使用[XEP-0082](http://xmpp.org/extensions/xep-0082.html)定义的 **UTC** 日期时间格式。

### 查询圈子成员列表

#### 例子1.用户查询特定圈子的成员列表

```
<iq from='user@skysea.com' to='100@group.skysea.com'  id='v3' type='get'>
  <query xmlns='http://skysea.com/protocol/group' node='members' />
</iq>
```

#### 例子2.服务返回圈子成员列表

```
<iq from='100@group.skysea.com'  to='user@skysea.com' id='v3' type='result'>
  <query xmlns='http://skysea.com/protocol/group' node='members'>
  	<x xmlns='jabber:x:data' type='result'>
			<reported>
				<field var='username'/>
				<field var='nickname'/>
	      	</reported>
			<item>
				<field var='username'> <value>user1</value> </field>
				<field var='nickname'> <value>小李飞刀</value> </field>
		    </item>
		    .
		    [more items]
		    .
		    <item>
				<field var='username'> <value>user10</value> </field>
				<field var='nickname'> <value>大刀关胜</value> </field>
		    </item>
	</x>
  </query>
</iq>
```

### 查询已加入的圈子列表

#### 例子1.用户查询已加入的圈子列表

```
<iq from='user@skysea.com' to='group.skysea.com' id='v4' type='get'>
	<query xmlns='http://skysea.com/protocol/group#user' node='groups' />
</iq>
```

#### 例子2.服务返回用户加入的所有圈子列表

```
<iq from='group.skysea.com' to='user@skysea.com' id='v4' type='result'>
	<query xmlns='http://skysea.com/protocol/group#user' node='groups' >
        <x xmlns='jabber:x:data' type='result'>
            <reported>
                <field var='jid'/>
            </reported>
            <item>
                <field var='jid'> <value>1@group.skysea.com</value> </field>
            </item>
            .
            [8 more items]
            .
            <item>
                <field var='jid'> <value>10@group.skysea.com</value> </field>
            </item>
        </x>
	</query>
</iq>
```

### 申请加入圈子

#### 例子1.用户申请加入圈子

```
<iq from='user@skysea.com' to='100@group.skysea.com' id='v5' type='set'>
  <x xmlns='http://skysea.com/protocol/group#user'>
  	<apply>
  		<member nickname='碧眼狐狸' />
  		<reason>我也是80后，请让我加入吧！</reason>
  	</apply>
  </x>
</iq>
```

`reason`是可选的附加消息，`nickname`是用户在圈子中使用的昵称。

#### 例子2.服务返回申请成功

```
<iq from='100@group.skysea.com' to='user@skysea.com' id='v5' type='result' />
```

用户发起申请之后服务会根据圈子的开放程度决定是直接将用户加入圈子，还是将申请转发给圈子所有者，由圈子所有者决定。

#### 例子3.服务返回申请失败：人数已经达到最大上限

```
<iq from='100@group.skysea.com' to='user@skysea.com' id='v5' type='error'>
	<error type='wait'>
    	<service-unavailable xmlns='urn:ietf:params:xml:ns:xmpp-stanzas'/>
	</error>
</iq>
```

#### 例子4.服务将用户的申请转发给圈子所有者

```
<message from='100@group.skysea.com' to='owner@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#owner'>
  	<apply id='s2fd1'>
  		<member username='user' nickname='碧眼狐狸' />
  		<reason>我也是80后，请让我加入吧！</reason>
  	</apply>
  </x>
</message>
```
圈子所有者客户端程序收到经由服务转发的申请之后，应当显示适当的UI元素方便圈子所有者做出决定拒绝或是同意用户的申请。

#### 例子5.圈子所有者处理申请：同意用户加入

```
<iq from='owner@skysea.com' to='100@group.skysea.com' id='v6' type='set'>
  <x xmlns='http://skysea.com/protocol/group#owner'>
  	<apply id='s2fd1'>
  		<agree />
  		<member username='user' nickname='碧眼狐狸' />
  		<reason>欢迎加入</reason>
  	</apply>
  </x>
</iq>
```
#### 例子6.圈子所有者处理申请：拒绝用户加入

```
<iq from='owner@skysea.com' to='100@group.skysea.com' id='v6' type='set'>
  <x xmlns='http://skysea.com/protocol/group#owner'>
  	<apply id='s2fd1'>
  		<decline />
  		<member username='user' nickname='碧眼狐狸' />
  		<reason>目前不考虑新人加入，不好意思！</reason>
  	</apply>
  </x>
</iq>
```
**注意： 圈子所有者向服务发送拒绝/同意应答消息时`apply`元素的`id`和`from`属性是必须按原样返回的。**

#### 例子7.服务向所有者返回处理申请成功

```
<iq from='100@group.skysea.com' to='owner@skysea.com' id='v6' type='result' />
```

#### 例子8.服务向申请者转发：所有者已同意申请

```
<message from='100@group.skysea.com' to='user@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#user'>
  	<apply>
		<agree from='owner@skysea.com' />
    	<reason>欢迎加入！</reason>
    </apply>
  </x>
</message>
```
`agree`包含可选的`from`属性，如果申请是经由所有者处理的则值为所有者jid。
`reason`是可选的消息元素，申请通过时可能是欢迎消息，申请被拒绝时可能是拒绝的原因。

#### 例子8.服务向申请者转发：所有者已拒绝申请

```
<message from='100@group.skysea.com' to='user@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#user'>
  	<apply>
    	<decline from='owner@skysea.com' />
    	<reason>目前不考虑新人加入，不好意思！</reason>
    </apply>
  </x>
</message>
```
`decline`的`from`属性表明是`owner@skysea.com`拒绝了用户的申请。
可选的`reason`显示了被拒绝的原因。



#### 例子9.服务向所有圈子成员广播：新成员加入

```
<message from='100@group.skysea.com' to='user1@group.skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<join>
  		<member username='user' nickname='碧眼狐狸' />
  	</join>
  </x>
</message>

...

<message from='100@group.skysea.com' to='user10@group.skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<join>
  		<member username='user' nickname='碧眼狐狸' />
  	</join>
  </x>
</message>
```


## 圈子成员用例



### 发送圈子消息

#### 例子1.圈子成员向圈子发送消息

```
<message from='user@skysea.com' to='100@group.skysea.com' type='groupchat'>
  <body>大家好啊，一起出来喝酒吧！</body>
</message>
```

**注意：message的`type`属性为`groupchat`，说明这是一个圈子聊天消息。**

#### 例子2.服务向所有圈子成员广播消息


```
<message from="100@group.skysea.com/user" to='user1@skysea.com' type='groupchat'>
  <body>大家好啊，一起出来喝酒吧！</body>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<member nickname='碧眼狐狸' />
  </x>
</message>

...

<message from="100@group.skysea.com/user" to='user10@skysea.com' type='groupchat'>
  <body>大家好啊，一起出来喝酒吧！</body>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<member nickname='碧眼狐狸' />
  </x>
</message>
```

当圈子成员接收到消息时`from`属性已被重写为圈子的`JID`，通过`JID`的`resource`值可知消息的发送者
用户名为：user。扩展元素 **x** 中的`member`元素包含发送者的昵称信息。


### 修改圈子名片

#### 例子1.圈子成员修改圈子名片信息

```
<iq from='user@skysea.com' to='100@group.skysea.com' id='v7' type='set'>
  <x xmlns='http://skysea.com/protocol/group#member'>
	<profile>
		<nickname>金轮法王</nickname>
	</profile>
 </x>
</iq>
```


#### 例子2.服务返回修改成功

```
<iq from='100@group.skysea.com' to='user@skysea.com' id='v7' type='result'>
</iq>
```


#### 例子3.服务向所有圈子成员广播：用户修改了圈子名片

```
<message from='100@group.skysea.com' to='user1@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<profile>
  		<member username='user' nickname='碧眼狐狸' />
  		<nickname>金轮法王</nickname>
  	</profile>
  </x>
</message>

...

<message from='100@group.skysea.com' to='user10@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<profile>
  		<member username='user' nickname='碧眼狐狸' />
  		<nickname>金轮法王</nickname>
  	</profile>
  </x>
</message>

```

### 邀请用户

#### 例子1.圈子成员邀请用户加入圈子

```
<iq from='user@skysea.com' to='100@group.skysea.com' id='v8' type='set'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<invite>
  		<member username='user100' nickname='独孤求败' />
  		<member username='user101' nickname='雁过留声' />
  		<member username='user102' nickname='圆月弯刀' />
  	</invite>
  </x>
</iq>
```

#### 例子2.服务返回邀请成功

```
<iq from='100@group.skysea.com' to='user@skysea.com' id='v8' type='result' />
```

#### 例子3.所有圈子成员收到用户被邀请的广播

```
<message from='100@group.skysea.com' to='user1@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<invite from='user@skysea.com'>
  		<member username='user100' nickname='独孤求败' />
  		<member username='user101' nickname='雁过留声' />
  		<member username='user102' nickname='圆月弯刀' />
  	</invite>
  </x>
</message>

...

<message from='100@group.skysea.com' to='user10@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<invite from='user@skysea.com'>
  		<member username='user100' nickname='独孤求败' />
  		<member username='user101' nickname='雁过留声' />
  		<member username='user102' nickname='圆月弯刀' />
  	</invite>
  </x>
</message>

```

### 退出圈子

#### 例子1.圈子成员退出圈子

```
<iq from='user@skysea.com' to='100@group.skysea.com' id='v9' type='set'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<exit>
  		<reason>大家太吵了，不好意思，我退了先！</reason>
  	</exit>
  </x>
</iq>
```

#### 例子2.服务返回退出成功

```
<iq from='100@group.skysea.com' to='user@skysea.com' id='v9' type='result' />
```

#### 例子3.服务向所有圈子成员广播：成员退出

```
<message from='100@group.skysea.com' to='user1@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<exit>
  		<member username='user' nickname='碧眼狐狸' />
  		<reason>大家太吵了，不好意思，我退了先！</reason>
  	</exit>
  </x>
</message>

...

<message from='100@group.skysea.com' to='user10@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<exit>
  		<member username='user' nickname='碧眼狐狸' />
  		<reason>大家太吵了，不好意思，我退了先！</reason>
  	</exit>
  </x>
</message>

```

## 圈子所有者用例


### 创建圈子


#### 例子1.用户提交创建表单

```
<iq from='user@skysea.com' to='group.skysea.com' id='v10' type='set'>
	<x xmlns='http://skysea.com/protocol/group'>
		<x xmlns='jabber:x:data' type='submit'>
		    <field var='name' type='text-single'>
		      <value>圈子名称</value>
		    </field>
		    <field var='logo' type='text-single'>
              <value>圈子logo图片</value>
            </field>
		    <field var='category' type='list-single'>
		      <value>1</value>
		    </field>
		    <field var='subject' type='text-single'>
		      <value>圈子主题</value>
		    </field>
		    <field var='description' type='text-multi'>
		      <value>圈子描述</value>
		    </field>
		    <field var='openness' type='list-single'>
		      <value>PUBLIC</value>
		    </field>
	  	</x>
  	</x>
</iq>
```
**圈子分类定义**

值		| 说明
-------	|-----------
1		| 兴趣爱好
2		| 生活休闲
3		| 行业交流


**圈子开放程度定义**

值				| 说明
---------------	|-----------
PUBLIC			| 完全开放
AFFIRM_REQUIRED	| 需要审核
PRIVATE         | 私有

#### 例子2.服务返回用户圈子创建成功

```
<iq from='group.skysea.com' to='user@skysea.com' id='v10' type='result'>
	<x xmlns='http://skysea.com/protocol/group'>
		<x xmlns='jabber:x:data' type='result'>
			<field var='jid'>
		      <value>100@group.skysea.com</value>
		    </field>
		</x>
	</x>
</iq>
```
服务返回表单中`jid`字段显示了服务为圈子自动生成的`jid`，圈子jid是进行圈子通信功能的目标地址。

### 修改圈子信息

#### 例子1.用户提交修改表单

```
<iq from='user@skysea.com' to='100@group.skysea.com' id='v11' type='set'>
	<x xmlns='http://skysea.com/protocol/group'>
		<x xmlns='jabber:x:data' type='submit'>
		    <field var='name'  type='text-single'>
		      <value>圈子名称</value>
		    </field>
            <field var='logo' type='text-single'>
              <value>圈子logo图片</value>
            </field>
		    <field var='category' type='list-single'>
		      <value>23</value>
		    </field>
		    <field var='subject' type='text-single'>
		      <value>圈子主题</value>
		    </field>
		    <field var='description' type='text-multi'>
		      <value>圈子描述</value>
		    </field>
		    <field var='openness' type='list-single'>
		      <value>PUBLIC</value>
		    </field>
	  	</x>
  	</x>
</iq>
```

#### 例子2.服务返回修改成功

```
<iq from='100@group.skysea.com' to='user@skysea.com' id='v11' type='result'>
</iq>
```

#### 例子3.圈子成员收到圈子信息被修改的广播

```
<message from='100@group.skysea.com' to='user1@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group'>
  	<change from='user@skysea.com' />
  </x>
</message>

...

<message from='100@group.skysea.com' to='user10@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group'>
  	<change from='user@skysea.com' />
  </x>
</message>

```


### 踢人

#### 例子1.圈子所有者提出踢出成员

```
<iq from='owner@skysea.com' to='100@group.skysea.com' id='v12' type='set'>
  <x xmlns='http://skysea.com/protocol/group#owner'>
  	<kick username='user'>
  		<reason>抱歉！你总是发送广告信息。</reason>
  	</kick>
  </x>
</iq>
```

#### 例子2.服务返回圈子踢出成功

```
<iq from='100@group.skysea.com' to='owner@skysea.com' id='v12' type='result'>
</iq>
```

#### 例子3.服务向所有圈子成员广播：成员被踢出
```
<message from='100@group.skysea.com' to='user1@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<kick from='owner@skysea.com'>
  		<member username='user' nickname='碧眼狐狸' />
  		<reason>抱歉！你总是发送广告信息。</reason>
  	</kick>
  </x>
</message>

...

<message from='100@group.skysea.com' to='user10@skysea.com'>
  <x xmlns='http://skysea.com/protocol/group#member'>
  	<kick from='owner@skysea.com'>
  		<member username='user' nickname='碧眼狐狸' />
  		<reason>抱歉！你总是发送广告信息。</reason>
  	</kick>
  </x>
</message>

```

`x`元素的`from`属性说明成员是被`owner@skysea.com`踢出的，

`reason`说明被踢出的原因，但它只对被踢出成员发送。


### 销毁圈子

#### 例子1.圈子所有者提交销毁圈子

```
<iq from='owner@skysea.com' to='100@group.skysea.com' id='v13' type='set'>
  <x xmlns='http://skysea.com/protocol/group#owner'>
  	<destroy>
  		<reason>再见了各位！</reason>
  	</destroy>
  </x>
</iq>
```
#### 例子2.服务返回销毁成功

```
<iq from='100@group.skysea.com' to='owner@skysea.com' id='v13' type='result'>
</iq>
```
#### 例子3.服务向圈子成员广播：圈子已被销毁

```
<message from='100@group.skysea.com' to='user1@skysea.com'>
   <x xmlns='http://skysea.com/protocol/group'>
  	<destroy from='owner@skysea.com'>
  		<reason>再见了各位！</reason>
  	</destroy>
  </x>
</message>

...

<message from='100@group.skysea.com' to='user10@skysea.com'>
   <x xmlns='http://skysea.com/protocol/group'>
  	<destroy from='owner@skysea.com'>
  		<reason>再见了各位！</reason>
  	</destroy>
  </x>
</message>
```


## 参考资料

[XEP-0004: Data Forms](http://xmpp.org/extensions/xep-0004.html) 
	([中文](http://wiki.jabbercn.org/XEP-0004))
	
[XEP-0059: Result Set Management](http://xmpp.org/extensions/xep-0059.html) 

[XEP-0082:XMPP Date and Time Profiles](http://xmpp.org/extensions/xep-0082.html)

[XEP-0055: Jabber Search](http://xmpp.org/extensions/xep-0055.html)

[XEP-0203: Delayed Delivery](http://xmpp.org/extensions/xep-0082.html)

	

