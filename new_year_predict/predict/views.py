import json
import time

from django.http import HttpResponse

# Create your views here.
from django.views.decorators.csrf import csrf_exempt
import det_keypoint_unite_infer2 as keypoint


@csrf_exempt
def base64(request):
    res = {}
    if request.method == 'GET':
        res['message'] = keypoint.main()
        return HttpResponse(json.dumps(res))
    else:
        json_str = request.body
        body = json.loads(json_str)
        img = body.get("img")
        data = keypoint.base64(img)
        res['message'] = 'success'
        res['code'] = 20000
        res['data'] = data
        return HttpResponse(json.dumps(res))


@csrf_exempt
def array_buffer(request):
    res = {}
    if request.method == 'GET':
        res['message'] = keypoint.main()
        return HttpResponse(json.dumps(res))
    else:
        print(time.time())
        json_str = request.body
        body = json.loads(json_str)
        img_array = body['imgArray']
        width = body['width']
        height = body['height']
        data = keypoint.array_buffer(img_array, width, height)
        print(time.time())
        res['message'] = 'success'
        res['code'] = 20000
        res['data'] = data
        return HttpResponse(json.dumps(res))
