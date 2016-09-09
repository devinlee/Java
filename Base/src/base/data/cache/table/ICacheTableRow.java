package base.data.cache.table;

import java.util.concurrent.ConcurrentHashMap;

import base.event.IEventListener;
import base.types.JavaType;

public interface ICacheTableRow
{
	/**
	 * 过期时间
	 * @return expiresTime
	 */
	public long getExpiresTime();

	/**
	 * 过期时间
	 * @param expiresTime 要设置的 expiresTime
	 */
	public void setExpiresTime(long expiresTime);

	/**
	 * 链表上一个对象
	 */
	public ICacheTableRow getPrevious();
	
	/**
	 * 链表上一个对象
	 */
	public void setPrevious(ICacheTableRow cacheTableRow);
	
	/**
	 * 链表下一个对象
	 */
	public ICacheTableRow getNext();

	/**
	 * 链表下一个对象
	 */
	public void setNext(ICacheTableRow cacheTableRow);
	
	/**
	 * 表行字段集
	 * @return 表行字段集
	 */
	public ConcurrentHashMap<String, ICacheTableField> getFields();
	
	/**
	 * 是否为新创建的行数据(对应的数据库不存在该条数据，或者没有向数据库存储过)
	 * @return
	 */
	public boolean getIsNewRow();
	
	/**
	 * 是否为新创建的行数据(对应的数据库不存在该条数据，或者没有向数据库存储过)
	 * @param value 要设置的值
	 */
	public void setIsNewRow(boolean value);
	
	/**
	 * 行数据发生改变
	 * @return
	 */
	public void change();
	
	/**
	 * 行数据发生改变
	 * @param saveChangeSaveFlag 是否立即保存其行字段中“isChangeSaveFlag”为true的字段
	 */
	public void change(boolean saveChangeSaveFlag);

	/**
	 * 增加行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param fieldData 字段内容数据
	 * @param fieldJavaType 字段数据类型
	 * @return 如果增加成功返回true,否则返回false
	 */
	public <T> boolean addField(String fieldName, T fieldData);

	/**
	 * 增加行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param fieldData 字段内容数据
	 * @param fieldJavaType 字段数据类型
	 * @return 如果增加成功返回true,否则返回false
	 */
	public <T> boolean addField(String fieldName, T fieldData, JavaType fieldJavaType);

	/**
	 * 增加行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param cacheTableField 字段，值不能为空
	 * @return 如果增加成功返回true,否则返回false
	 */
	public boolean addField(String fieldName, ICacheTableField cacheTableField);

	/**
	 * 增加或更新行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param fieldData 字段内容数据
	 * @return 如果增加成功返回true,否则返回false
	 */
	public <T> boolean addOrUpdateField(String fieldName, T fieldData);

	/**
	 * 增加或更新行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param fieldData 字段内容数据
	 * @param fieldJavaType 字段数据类型
	 * @return 如果增加成功返回true,否则返回false
	 */
	public <T> boolean addOrUpdateField(String fieldName, T fieldData, JavaType fieldJavaType);

	/**
	 * 增加或更新行字段单元数据
	 * @param fieldName 字段名，值不能为空
	 * @param cacheTableField 字段
	 * @return 如果增加成功返回true,否则返回false
	 */
	public boolean addOrUpdateField(String fieldName, ICacheTableField cacheTableField);

	/**
	 * 取得字段单元
	 * @param fieldName 字段名
	 * @return 字段单元，如果字段单元为null，则返回null
	 */
	public ICacheTableField getField(String fieldName);

	/**
	 * 取得字段单元数据
	 * @param fieldName 字段名
	 * @return 字段单元数据，如果对应的字段单元为null，则返回null
	 */
	public <T> T getFieldData(String fieldName);

	/**
	 * 取得行字段数据是否被更改状态
	 * @param isChange 更改状态：true为有更改,false为没有更改
	 * @return 只要行其中存在一个字段被修改，返回true,行所有字段都未被修改过，则返回false
	 */
	public boolean getFieldChangeState();

	/**
	 * 检查是否存在指定的字段
	 * @param fieldName 字段名
	 * @return 如果存返回true，否则返回false
	 */
	public boolean containsField(String fieldName);

	/**
	 * 立即通知表进行数据物理存储
	 */
	public void toSave();
	
	/*
	 * 克隆，深度复制一份全新的数据,数据内容仅为行字段数据集，其它行属性则为默认值
	 */
	public ICacheTableRow clone();
	
	/**
	 * 将相同字段的数据复制至目标行
	 * @param desCacheTableRow 目标行
	 */
	public void copy(ICacheTableRow desCacheTableRow);
	
	/**
	 * 增加事件监听
	 * @param type 事件类型
	 * @param eventListener 事件接收者
	 * @return
	 */
	public void addEventListener(String type, IEventListener eventListener);

	/**
	 * 是否存在指定的事件监听
	 * @param type 事件类型
	 * @param eventListener 事件接收者
	 * @return
	 */
	public boolean hasEventListener(String type, IEventListener eventListener);

	public void removeEventListener(String type, IEventListener eventListener);

	public void dispose();
}