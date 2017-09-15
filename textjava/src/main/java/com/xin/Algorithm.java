package com.xin;

import java.util.List;
import java.util.Random;

/**
 * Created by xiaoxin on 17-8-21.
 */

public class Algorithm {
    public static void main(String... args) {
        int[] a = {9, 8, 7, 9, 5, 4, 3, 9, 1, 0};
        int[] b = {49, 38, 65, 97, 176, 213, 227, 49, 78, 34, 12, 164, 11, 18, 1};
        int[] c = {50, 2, 13, 100, 101, 12, 14};
        int[] d = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        int[] e = AlgorithmInternal.getIntArray(20);
        /*int[] nums = new int[a.length];
        System.arraycopy(a, 0, nums, 0, a.length);*/
        int[] nums = e;

        System.out.print("Sort nums before: ");
        AlgorithmInternal.print(nums);

        //交换排序（冒泡排序，快速排序）
        //AlgorithmInternal.bubbleSort(nums);
        //AlgorithmInternal.quickSort1(nums, 0, nums.length - 1);
        //AlgorithmInternal.quickSort2(nums, 0, nums.length - 1);

        //选择排序（直接选择排序，堆排序）
        //AlgorithmInternal.selectSort(nums);
        //AlgorithmInternal.heapSort(nums);

        //插入排序（直接插入排序，折半插入排序，Shell排序）
        //AlgorithmInternal.insertSort(nums);
        //AlgorithmInternal.binaryInsertSort(nums);
        //AlgorithmInternal.shellSort(nums);

        //归并排序
        AlgorithmInternal.mergeSort(nums, 0, nums.length -1);

        //桶排序
        //AlgorithmInternal.bucketSort(nums, 100);

        //基数排序
        //AlgorithmInternal.radixSort(nums);

        System.out.print("\nSort nums end: ");
        AlgorithmInternal.print(nums);
    }
}

class AlgorithmInternal {

    public static void swap1(int[] data, int i, int j) {
        if (i == j || data[i] == data[j]) return;
        int temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    public static void swap2(int[] data, int i, int j) {
        if (i == j || data[i] == data[j]) return;
        if ((data[i] > 0 && data[j] > 0) || (data[i] < 0 && data[j] < 0)) {
            //data[i]=data[i]-data[j]; //x = x - y
            //data[j]=data[i]+data[j]; //y = x + y
            //data[i]=data[j]-data[i]; //x = y - x
            data[j] = data[i] - data[j] + (data[i] = data[j]); //y = x - y + (x = y);
        } else {
            //data[i]=data[i]+data[j]; // x = x + y
            //data[j]=data[i]-data[j]; // y = x - y
            //data[i]=data[i]-data[j]; // x = x - y  这里与上面不同，使用的是: x - y
            data[j] = data[i] + data[j] - (data[i] = data[j]); //y = x + y - (x = y);
        }
    }

    public static void swap3(int[] data, int i, int j) {
        if (i == j || data[i] == data[j]) return;
        int a = data[i];   //由于数组进来的是地址，所以不能直接使用data[i]来异或data[j]
        int b = data[j];
        a ^= b;
        b ^= a;
        a ^= b;
        //a ^= b ^= a ^= b;
        data[i] = a;
        data[j] = b;
    }

    //1.1 交换排序_冒泡排序
    public static void bubbleSort(int[] numbers) {
        int size = numbers.length; // 数组大小
        for (int i = 0; i < size - 1; i++) {  //从 0 到 size-1
            for (int j = i + 1; j < size; j++) { //从 i+1 到 size
                if (numbers[i] > numbers[j]) { // 交换两数的位置
                    swap1(numbers, i, j);
                }
            }
        }
    }

    //1.2 交换排序_快速排序
    public static void quickSort1(int[] numbers, int start, int end) {
        if (start < end) {
            int base = numbers[start]; // 选定的基准值（第一个数值作为基准值）
            int i = start, j = end;
            do {
                while ((numbers[i] < base) && (i < end))
                    i++;
                while ((numbers[j] > base) && (j > start))
                    j--;
                if (i <= j) {
                    if (i != j && numbers[i] != numbers[j]) {
                        swap1(numbers, i, j);
                    }
                    i++;
                    j--;
                }
            } while (i < j);

            if (start < j)
                quickSort1(numbers, start, j);
            if (i < end)
                quickSort1(numbers, i, end);
        }
    }

    public static void quickSort2(int[] numbers, int low, int high) {
        if (low < high) {
            int middle = getMiddle(numbers, low, high); //将numbers数组进行一分为二
            if (low < middle)
                quickSort2(numbers, low, middle - 1);   //对低字段表进行递归排序
            if (high > middle)
                quickSort2(numbers, middle + 1, high); //对高字段表进行递归排序
        }

    }

    public static int getMiddle(int[] numbers, int low, int high) {
        int temp = numbers[low]; //数组的第一个作为中轴
        while (low < high) {
            while (low < high && numbers[high] > temp) {
                high--;
            }
            if (low < high) {
                //swap1(numbers, low, high);//比中轴小的记录移到低端
                numbers[low] = numbers[high];
                low++;
            }

            while (low < high && numbers[low] < temp) {
                low++;
            }
            if (low < high) {
                //swap1(numbers, low, high); //比中轴大的记录移到高端
                numbers[high] = numbers[low];
                high--;
            }
        }
        //因为每次或增一或减一，最后结果必然是low == hight。
        //System.out.println("low: "+low+",   high: "+high);
        numbers[low] = temp; //中轴记录到尾
        return low; // 返回中轴的位置
    }

    //2.1 选择排序_直接选择排序
    public static void selectSort(int[] numbers) {
        int size = numbers.length, temp;
        for (int i = 0; i < size; i++) { //从 0 到 size
            int k = i; //k：记录剩余数组（i 到 size-1）中最小数的指标
            for (int j = i + 1; j < size; j++) //从 i+1 到 size
                if (numbers[j] < numbers[k]) k = j;
            swap2(numbers, i, k);
        }
    }

    //2.2 选择排序_堆排序
    public static void heapSort(int[] data) {
        for (int i = 0; i < data.length; i++) {
            createMaxdHeap(data, data.length - 1 - i);
            swap1(data, 0, data.length - 1 - i);
            //print(data);
        }
    }

    public static void createMaxdHeap(int[] data, int lastIndex) {
        for (int i = (lastIndex - 1) / 2; i >= 0; i--) {
            int k = i; // 保存当前正在判断的节点
            while (2 * k + 1 <= lastIndex) { // 若当前节点的子节点存在
                int biggerIndex = 2 * k + 1; // biggerIndex总是记录较大节点的值,先赋值为当前判断节点的左子节点
                if (biggerIndex < lastIndex) {
                    if (data[biggerIndex] < data[biggerIndex + 1]) { // 若右子节点存在，否则此时biggerIndex应该等于 lastIndex
                        biggerIndex++; // 若右子节点值比左子节点值大，则biggerIndex记录的是右子节点的值
                    }
                }
                if (data[k] < data[biggerIndex]) {
                    swap1(data, k, biggerIndex); // 若当前节点值比子节点最大值小，则交换二者的值，交换后将biggerIndex值赋值给k
                    k = biggerIndex;
                } else {
                    break;
                }
            }
        }
    }

    //3.1 插入排序_直接插入排序
    public static void insertSort(int[] numbers) {
        int size = numbers.length, temp, j;
        for (int i = 1; i < size; i++) { //从 1 到 size
            temp = numbers[i];
            for (j = i; j > 0 && temp < numbers[j - 1]; ) //从 i 向左到 小等于 temp 的位置，只要比temp大就互换位置
                numbers[j--] = numbers[j];
            numbers[j] = temp;
        }
    }

    //3.2 插入排序_折半插入排序
    public static void binaryInsertSort(int[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int temp = arr[i]; //待插入元素
            int low = 0, high = i - 1; //已排序好数组的首末指标
            while (low <= high) { //使用二分查找，找到带插入位置 low
                int mid = (low + high) / 2;
                if (arr[mid] > temp)
                    high = mid - 1;    //在小于中间值的那部分寻找
                else
                    low = mid + 1;     //在大于中间值的那部分寻找
            }
            for (int j = i; j > low; j--) { //整体数组后移,到 j = low
                arr[j] = arr[j - 1];
            }
            arr[low] = temp;
        }
    }

    //3.3 插入排序_希尔排序
    public static void shellSort(int[] arr) {
        int len = arr.length;
        int x, i, j; //x:分组后第几个数组; i,j:实现单个数组的直接插入排序
        while (true) {
            len = len / 2; //分数组使用, 对半开, 直到1(即不分了，对整个原始数组执行简单插入排序)
            for (x = 0; x < len; x++) { //第x数组的直接插入排序
                for (i = x + len; i < arr.length; i += len) { //相当于从1到size
                    int temp = arr[i];
                    for (j = i - len; j >= 0 && arr[j] > temp; j -= len) { //相当于从0到i
                        arr[j + len] = arr[j];
                    }
                    arr[j + len] = temp;
                }
            }
            if (len == 1) break;
        }
    }

    //4 归并排序
    public static void mergeSort(int[] data, int left, int right) {
        if (left >= right) return;
        int center = (left + right) / 2; //找出中间索引
        mergeSort(data, left, center); //对左边数组进行递归
        mergeSort(data, center + 1, right); //对右边数组进行递归
        merge(data, left, center, right); //合并
        //print(data);
    }

    public static void merge(int[] data, int left, int center, int right) {
        //int[] tempArray = new int[data.length]; // 建立 临时数组 长度data.length, 以下“ - tempLeft”可以去掉
        int[] tempArray = new int[right - left + 1]; // 建立 临时数组 长度为right - left + 1
        final int tempLeft = left; //固定本次操作在原始数组的起始位置
        int rightStart = center + 1; // rightStart：右数组第一个元素的索引
        int index = left; // index：记录数组的索引(变化范围是[left, right])
        // 从两个数组中取出最小的放入临时数组
        while (left <= center && rightStart <= right)
            if (data[left] <= data[rightStart])
                tempArray[index++ - tempLeft] = data[left++];
            else
                tempArray[index++ - tempLeft] = data[rightStart++];
        // 剩余部分依次放入临时数组（实际上两个while只会执行其中一个）
        while (left <= center)
            tempArray[index++ - tempLeft] = data[left++];
        while (rightStart <= right)
            tempArray[index++ - tempLeft] = data[rightStart++];
        // 将临时数组中的内容拷贝回原数组中（原left-right范围的内容被复制回原数组）
        while (--index >= tempLeft)
            data[index] = tempArray[index - tempLeft];
    }

    //5 桶排序
    //只能对正数数组排序，而且最好数组中最大值不要太大
    public static void bucketSort(int[] a, int max) { //max: 对应数组a中元素的取值范围[0,max)
        if (a == null || max < 1) return;
        int[] buckets = new int[max]; // 1 创建一个容量为max的数组buckets，并且将buckets中的所有数据都初始化为0。
        for (int i = 0; i < a.length; i++) // 2 计数
            buckets[a[i]]++;
        for (int i = 0, j = 0; i < max; i++) // 3 排序
            while (buckets[i]-- > 0)
                a[j++] = i;
        buckets = null;
    }

    //6 基数排序
    public static void radixSort(int[] array) {
        int n = 1;//代表位数对应的数：1,10,100...
        int index = 0;//排位前,指向数组首
        int length = array.length;
        int[][] bucket = new int[10][length];//排序桶用于保存每次排序后的结果，这一位上排序结果相同的数字放在同一个桶里
        int[] order = new int[length];//order[i]: bucket[i]里保存了order[i]条记录
        int max = array[0];
        for (int t : array) if (max < t) max = t; //获取数组最大值
        while (n < max) {
            for (int num : array) { //将数组array里的每个数字放在相应的桶里
                int digit = (num / n) % 10;
                bucket[digit][order[digit]++] = num;
            }
            for (int i = 0; i < length; i++) { //将前一个循环生成的桶里的数据覆盖到原数组中用于保存这一位的排序结果
                if (order[i] != 0)//这个桶里有数据，从上到下遍历这个桶并将数据保存到原数组中
                    for (int j = 0; j < order[i]; j++)
                        array[index++] = bucket[i][j];
                order[i] = 0;//将桶里计数器置0，用于下一次位排序
            }
            n *= 10;
            index = 0;
            //print(array);
        }
    }

    //洗牌：将N个数乱序后输出
    public static void shuffleSort(int[] data) {
        for (int i = 0; i < data.length - 1; i++) {
            int j = (int) (data.length * Math.random());
            swap1(data, i, j);
        }
    }

    //插入操作:向大根堆array中插入数据data
    public int[] insertData(int[] array, int data) {
        array[array.length - 1] = data; //将新节点放在堆的末端
        int k = array.length - 1;  //需要调整的节点
        int parent = (k - 1) / 2;    //双亲节点
        while (parent >= 0 && data > array[parent]) {
            array[k] = array[parent];  //双亲节点下调
            k = parent;
            if (parent != 0) {
                parent = (parent - 1) / 2;  //继续向上比较
            } else {  //根节点已调整完毕，跳出循环
                break;
            }
        }
        array[k] = data;  //将插入的结点放到正确的位置
        return array;
    }

    public static int[] getIntArray(int n) {
        if (n <= 0) throw new ArrayIndexOutOfBoundsException();
        int[] arr = new int[n];
        Random r = new Random();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = r.nextInt(100);
        }
        return arr;
    }

    public static void print(int[] data) {
        for (int i = 0; i < data.length; i++) {
            System.out.print(data[i] + "  ");
        }
        System.out.println();
    }

    public static void printList(List<Integer> list) {
        while (list.size() > 0) {
            System.out.print(list.remove(0) + "\t");
        }
    }
}
