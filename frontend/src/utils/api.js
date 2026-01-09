import axios from 'axios'
import _ from 'lodash'
import { useErrorStore } from '@/stores/errorStore'

const BASE_API_URL = import.meta.env.VITE_APP_APIURL

axios.interceptors.request.use(
  (config) => config,
  (error) => Promise.reject(error),
)

axios.interceptors.response.use(
  (response) => response,

  async (error) => {
    try {
      const resp = error?.response;
      const data = resp?.data;
      const ct = resp?.headers?.["content-type"] ?? "";

      // If server sent JSON but we got a Blob (because responseType:'blob'), parse it
      if (data instanceof Blob && ct.includes("application/json")) {
        try {
          const text = await data.text();
          resp.data = JSON.parse(text); // replace only the data, keep the response shape the same
          (error)._blobJsonParsed = true; // optional flag if you want to detect this later
        } catch {
        }
      }

      // Check if ignoreError is set in the request config
      if (!resp?.config?.ignoreError) {
        // surface a user-friendly message without changing the response object structure
        const errorStore = useErrorStore();
        const msg =
          resp?.data?.errorMessage ||
          resp?.data?.message ||
          error.message ||
          "An error occurred while processing your request.";
        errorStore.showError(msg);
      }
    } catch {
      /* ignore store issues */
    }

    return Promise.reject(error);
  }
);

const api = {
  get(url, extendedOptions) {
    const request = {
      url: BASE_API_URL + url,
      method: 'get',
    }
    if (extendedOptions) {
      _.assign(request, extendedOptions)
    }
    return axios(request)
  },

  post(url, data, extendedOptions) {
    const options = {
      url: BASE_API_URL + url,
      method: 'post',
      data,
    }
    if (extendedOptions) {
      _.assign(options, extendedOptions)
    }
    return axios(options)
  },

  put(url, data, extendedOptions) {
  const options = {
    url: BASE_API_URL + url,
    method: 'put',
    data,
    }
    if (extendedOptions) {
      _.assign(options, extendedOptions)
    }
    return axios(options)
  },

  delete(url) {
    const request = {
      url: BASE_API_URL + url,
      method: 'delete',
    }
    return axios(request)
  },
}

export default api
